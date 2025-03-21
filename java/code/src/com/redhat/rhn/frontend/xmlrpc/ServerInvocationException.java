/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.localization.LocalizationService;

/**
 * ISS Slave we're trying to create already exists
 *
 */
public class ServerInvocationException extends FaultException {

    private static final long serialVersionUID = -736057155929214695L;

    /**
     * Tried to create a slave when one already exists w/a given name
     * @param fqdn the fqdn of the problematic server
     */
    public ServerInvocationException(String fqdn) {
        super(3002, "serverInvocationException", LocalizationService.getInstance().
                getMessage("api.iss.serverinvocationexception", fqdn));
    }

    /**
     * Tried to create a slave when one already exists w/a given name
     * @param fqdn the fqdn of the problematic server
     * @param cause what caused this exception
     */
    public ServerInvocationException(String fqdn, Throwable cause) {
        super(3002, "serverInvocationException", LocalizationService.getInstance().
            getMessage("api.iss.serverinvocationexception", fqdn), cause);


    }
}
