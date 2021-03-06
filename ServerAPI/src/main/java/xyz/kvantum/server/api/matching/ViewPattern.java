/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2017 IntellectualSites
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.matching;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.kvantum.server.api.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *  ViewPattern will be referred to as filter
 * </p>
 * <p>
 *  Filters are used to determine which view gets to serve the incoming requests.
 *  Each filter is made up of different parts, and there are four types of parts:
 *  <ul>
 *      <li>Separator - / - Used like a path separator</li>
 *      <li>Static - Example: user - A static string</li>
 *      <li>Required Variable - Example: &lt;username&gt;</li>
 *      <li>Optional Variable - Example: [page]</li>
 *  </ul>
 * </p>
 * <p>
 *     <b>Examples</b>
 * </p>
 * <p>
 *     <b>user/&lt;username&gt;</b> - Serves user/Citymonstret, but not user/ or user/Citymonstret/other
 * </p>
 * <p>
 *     <b>news/[page]</b> - Serves news, news/1, news/foo, but not news/foo/bar
 * </p>
 * <p>
 *     <b>user/&lt;username&gt;/posts/[page]</b> - Serves user/Citymonstret/posts and user/Citymonstrst/posts/10
 * </p>
 */
@SuppressWarnings("unused")
public class ViewPattern
{

    private static final Pattern PATTERN_VARIABLE_REQUIRED = Pattern.compile( "<([a-zA-Z0-9]*)>" );
    private static final Pattern PATTERN_VARIABLE_OPTIONAL = Pattern.compile( "\\[([a-zA-Z0-9]*)(=([a-zA-Z0-9]*))?]" );
    private static final Pattern PATTERN_VARIABLE_STATIC = Pattern.compile( "([a-zA-Z0-9]*)" );

    private final List<Part> parts = new ArrayList<>();
    private final String raw;

    /**
     * Generate a list of parts from the provided string
     * @param in String to compile
     */
    public ViewPattern(final String in)
    {
        Assert.notNull( in );

        this.raw = in;

        final SmartString string = new SmartString( raw.toLowerCase( Locale.ENGLISH ) );
        string.replaceLastIf( '/', SmartString.nil );
        string.replaceFirstIf( '/', SmartString.nil );

        final List<Integer> delimiterTypes = new ArrayList<>();
        for ( final Character c : string )
        {
            if ( c.equals( '.' ) )
            {
                delimiterTypes.add( 0 );
            } else if ( c.equals( '/' ) )
            {
                delimiterTypes.add( 1 );
            }
        }
        final Iterator<Integer> delimiterIterator = delimiterTypes.iterator();

        final StringTokenizer stringTokenizer = new StringTokenizer( string.toString(), "\\/." );
        while ( stringTokenizer.hasMoreTokens() )
        {
            final String token = stringTokenizer.nextToken();
            if ( token.isEmpty() )
            {
                continue;
            }
            Matcher matcher;
            if ( ( matcher = PATTERN_VARIABLE_REQUIRED.matcher( token ) ).matches() )
            {
                this.parts.add( new Variable( matcher.group( 1 ), Variable.TYPE_REQUIRED ) );
            } else if ( ( matcher = PATTERN_VARIABLE_OPTIONAL.matcher( token ) ).matches() )
            {
                if ( matcher.group( 3 ) == null || matcher.group( 3 ).isEmpty() )
                {
                    this.parts.add( new Variable( matcher.group( 1 ), Variable.TYPE_OPTIONAL ) );
                } else
                {
                    this.parts.add( new Variable( matcher.group( 1 ), Variable.TYPE_OPTIONAL, matcher.group( 3 ) ) );
                }
            } else if ( ( matcher = PATTERN_VARIABLE_STATIC.matcher( token ) ).matches() )
            {
                this.parts.add( new Static( matcher.group( 1 ) ) );
            }
            if ( stringTokenizer.hasMoreTokens() && delimiterIterator.hasNext() )
            {
                final int delimiterType = delimiterIterator.next();
                if ( delimiterType == 0 )
                {
                    this.parts.add( new Dot() );
                } else if ( delimiterType == 1 )
                {
                    this.parts.add( new Split() );
                }
            }
        }
    }

    /**
     * Test if a string matches the pattern
     * @param in String to test for
     * @return A map containing the variables extracted from the string.
     *         If there was no match, the map will be null
     */
    public Map<String, String> matches(final String in)
    {
        Assert.notNull( in );

        final SmartString url;
        if ( in.contains( "?" ) )
        {
            url = new SmartString( in.split( "\\?" )[ 0 ] );
        } else
        {
            url = new SmartString( in );
        }

        url.replaceFirstIf( '/', SmartString.nil );
        url.replaceLastIf( '/', SmartString.nil );

        if ( url.count( '.' ) >= 2 )
        {
            url.replaceAllButLast( '.', '§' );
        }

        if ( parts.isEmpty() )
        {
            if ( url.toString().isEmpty() )
            {
                return new HashMap<>();
            } else
            {
                return null;
            }
        }

        final List<Integer> delimiterTypes = new ArrayList<>();
        for ( final Character c : url )
        {
            if ( c.equals( '.' ) )
            {
                delimiterTypes.add( 0 );
            } else if ( c.equals( '/' ) )
            {
                delimiterTypes.add( 1 );
            }
        }

        final Iterator<Integer> delimiterIterator = delimiterTypes.iterator();
        int currentDelimiter = -1;

        final Map<String, String> variables = new HashMap<>();
        final StringTokenizer stringTokenizer = new StringTokenizer( url.toString(), "\\/." );
        Part lastPart = null;

        int iterator = -1;

        for ( final Part part : parts )
        {
            iterator++;

            if ( part instanceof Split || part instanceof Dot )
            {
                if ( delimiterIterator.hasNext() )
                {
                    currentDelimiter = delimiterIterator.next();
                } else
                {
                    currentDelimiter = -1;
                }
                lastPart = part;

                if ( ( iterator + 1 ) < parts.size() &&
                        ( ( parts.get( iterator + 1 ) instanceof Variable &&
                                ( ( (Variable) parts.get( iterator + 1 ) ).getType() ) == Variable.TYPE_REQUIRED ) ||
                                parts.get( iterator + 1 ) instanceof Static ) )
                {
                    if ( part instanceof Split && currentDelimiter != 1 )
                    {
                        return null;
                    } else if ( part instanceof Dot && currentDelimiter != 0 )
                    {
                        return null;
                    }
                }
                continue;
            }

            boolean has = stringTokenizer.hasMoreTokens();
            String next = has ? stringTokenizer.nextToken() : "";

            if ( part instanceof Variable )
            {
                Variable v = (Variable) part;

                if ( v.getType() == Variable.TYPE_REQUIRED )
                {
                    if ( !has )
                    {
                        return null;
                    }
                } else if ( has )
                {
                    if ( lastPart instanceof Split && currentDelimiter != 1 )
                    {
                        return null;
                    } else if ( lastPart instanceof Dot && currentDelimiter != 0 )
                    {
                        return null;
                    }
                }
            } else if ( part instanceof Static )
            {
                if ( !has )
                {
                    return null;
                } else
                {
                    if ( !next.equalsIgnoreCase( part.toString() ) )
                    {
                        return null;
                    }
                }
            }

            if ( part instanceof Variable )
            {
                final Variable variable = (Variable) part;
                if ( next.isEmpty() )
                {
                    if ( variable.hasDefaultValue() )
                    {
                        variables.put( variable.getName(), variable.getDefaultValue() );
                    }
                } else
                {
                    variables.put( variable.getName(), next );
                }
            }

            lastPart = part;
        }

        if ( stringTokenizer.hasMoreTokens() )
        {
            return null;
        }
        return variables;
    }

    @Override
    public String toString()
    {
        return this.raw;
    }

    private abstract static class Part
    {

        @Override
        public abstract String toString();

    }

    private static class Static extends Part
    {

        private final String string;

        private Static(final String string)
        {
            this.string = string;
        }

        @Override
        public String toString()
        {
            return string;
        }
    }

    @SuppressWarnings("unused")
    private static class SmartString implements Iterable<Character>
    {

        private static final char nil = '#';

        private char[] chars;
        private int length;

        private boolean changed = false;

        private SmartString(final String in)
        {
            Assert.notNull( in );

            this.chars = in.toCharArray();
            this.length = in.length();
        }

        char lastCharacter()
        {
            return chars[ length - 1 ];
        }

        void replaceLast(char c)
        {
            set( length - 1, c );
        }

        void replaceAll(char c, char w)
        {
            int[] indices = findAll( c );
            for ( int i : indices )
            {
                set( i, w );
            }
        }

        void replaceLastIf(char c, char k)
        {
            replaceIf( length - 1, c, k );
        }

        int count(char search)
        {
            int i = 0;
            for ( char c : this.chars )
            {
                if ( c == search )
                {
                    i += 1;
                }
            }
            return i;
        }

        int[] search(char search)
        {
            final int count = this.count( search );
            int[] indices = new int[ count ];
            char c;
            int indexPointer = 0;
            for ( int index = 0; index < this.chars.length; index++ )
            {
                c = this.chars[ index ];
                if ( c == search )
                {
                    indices[ indexPointer++ ] = index;
                }
            }
            return indices;
        }

        void replaceIf(int n, char c, char k)
        {
            if ( length == 0 )
            {
                return;
            }
            if ( chars[ n ] == c )
            {
                set( n, k );
            }
        }

        int[] findAll(char c)
        {
            int[] indices = new int[ length ];
            int written = 0;
            for ( int i = 0; i < length; i++ )
            {
                if ( chars[ i ] == c )
                {
                    indices[ written++ ] = i;
                }
            }
            int[] n = new int[ written ];
            System.arraycopy( indices, 0, n, 0, n.length );
            return n;
        }

        void set(final int i, final char c)
        {
            if ( length == 0 )
            {
                return;
            }
            chars[ i ] = c;
            changed = true;
        }

        void remove(final int i)
        {
            set( i, nil );
        }

        void regenerate()
        {
            char[] temp = new char[ length ];
            int index = 0;
            for ( char c : chars )
            {
                if ( c != nil )
                {
                    temp[ index++ ] = c;
                }
            }
            chars = new char[ index ];
            System.arraycopy( temp, 0, chars, 0, index );
            length = chars.length;
            changed = false;
        }

        @Override
        public String toString()
        {
            if ( changed )
            {
                regenerate();
            }
            return new String( chars );
        }

        @Override
        public Iterator<Character> iterator()
        {
            return new Iterator<Character>()
            {

                int index = 0;

                {
                    SmartString.this.regenerate();
                }

                @Override
                public boolean hasNext()
                {
                    return index < length;
                }

                @Override
                public Character next()
                {
                    return chars[ index++ ];
                }
            };
        }

        void replaceFirstIf(char c1, char c2)
        {
            replaceIf( 0, c1, c2 );
        }

        void replaceAllButLast(char search, char replacement)
        {
            final int[] indices = search( search );
            for ( int index = 0; index < indices.length - 1; index++ )
            {
                this.chars[ indices[ index ] ] = replacement;
            }
        }
    }

    private static final class Dot extends Part
    {

        @Override
        public String toString()
        {
            return ".";
        }
    }

    private static final class Split extends Part
    {

        @Override
        public String toString()
        {
            return "/";
        }
    }

    @AllArgsConstructor
    private static class Variable extends Part
    {

        private static int TYPE_REQUIRED = 0, TYPE_OPTIONAL = 1;
        @Getter
        private final String name;
        @Getter
        private final int type;
        @Getter
        private final String defaultValue;

        Variable(String name, int type)
        {
            this( name, type, null );
        }

        public boolean hasDefaultValue()
        {
            return this.getDefaultValue() != null;
        }

        @Override
        public String toString()
        {
            return this.name + ( type == TYPE_REQUIRED ? "" : "?" );
        }
    }

}
