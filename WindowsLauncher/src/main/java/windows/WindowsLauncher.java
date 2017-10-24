/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package windows;

import com.github.intellectualsites.iserver.api.util.AutoCloseable;
import com.github.intellectualsites.iserver.implementation.IntellectualServerMain;
import dorkbox.systemTray.SystemTray;

public class WindowsLauncher
{

    public static void main(final String[] args) throws Throwable
    {
        final SystemTray systemTray = SystemTray.get();
        if ( systemTray != null )
        {
            try
            {
                new SystemTrayIcon( systemTray ).setup();
            } catch ( Throwable throwable )
            {
                throwable.printStackTrace();
            }
        }
        IntellectualServerMain.main( args );
    }

    private static class SystemTrayIcon extends AutoCloseable
    {

        private final SystemTray systemTray;

        private SystemTrayIcon(final SystemTray systemTray)
        {
            this.systemTray = systemTray;
        }

        private void setup() throws Throwable
        {
            systemTray.setImage( WindowsLauncher.class.getResourceAsStream( "logo.png" ) );
            systemTray.setStatus( "Running" );
            systemTray.setTooltip( "IntellectualServer" );
            systemTray.setEnabled( true );
        }

        @Override
        protected void handleClose()
        {
            systemTray.shutdown();
        }
    }

}