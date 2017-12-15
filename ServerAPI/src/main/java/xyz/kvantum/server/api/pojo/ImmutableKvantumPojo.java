package xyz.kvantum.server.api.pojo;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.Map;

/**
 * Just like {@link KvantumPojo} but without any setters
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings({ "unused", "WeakerAccess" })
public final class ImmutableKvantumPojo<Pojo>
{

    @NonNull
    private final Pojo instance;
    @NonNull
    private final Map<String, PojoGetter<Pojo>> fieldValues;

    /**
     * Get a specified value. Will throw exceptions
     * if no such key is stored.
     *
     * @param key Key
     * @return Object
     * @see #containsGetter(String) To check if a key is stored
     */
    @SneakyThrows(NoSuchMethodException.class)
    public Object get(@NonNull final String key)
    {
        if ( !this.containsGetter( key ) )
        {
            throw new NoSuchMethodException( "No such getter: " + key );
        }
        return fieldValues.get( key ).get( this.instance );
    }

    /**
     * Get the names of all getters in the POJO
     *
     * @return Collection of field names
     */
    public Collection<String> getGetterNames()
    {
        return this.fieldValues.keySet();
    }

    /**
     * Check if this instance contains a given key
     *
     * @param key Key to check for
     * @return True if the key exists
     */
    public boolean containsGetter(@NonNull final String key)
    {
        return fieldValues.containsKey( key );
    }

    /**
     * Get the values for all getters in the POJO
     *
     * @return Immutable map with all values
     */
    public Map<String, Object> getAll()
    {
        final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
        for ( final Map.Entry<String, PojoGetter<Pojo>> getterEntry : this.fieldValues.entrySet() )
        {
            mapBuilder.put( getterEntry.getKey(), getterEntry.getValue().get( instance ) );
        }
        return mapBuilder.build();
    }

    /**
     * Construct a {@link JSONObject} from this instance
     *
     * @return JSON object
     */
    public JSONObject toJson()
    {
        return new JSONObject( this.getAll() );
    }

    /**
     * Get the POJO object represented by this class
     *
     * @return POJO instance
     */
    public Pojo getPojo()
    {
        return this.instance;
    }

    @Override
    public String toString()
    {
        return this.instance.toString();
    }

    @Override
    public int hashCode()
    {
        return this.instance.hashCode();
    }

    @Override
    public boolean equals(final Object object)
    {
        if ( object == null )
        {
            return false;
        } else if ( object instanceof KvantumPojo )
        {
            return this.instance.equals( ( (KvantumPojo) object ).getPojo() );
        } else
        {
            return this.instance.equals( object );
        }
    }
}