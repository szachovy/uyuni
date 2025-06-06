/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.frontend.action.token.groups;

import static com.redhat.rhn.manager.user.UserManager.ensureRoleBasedAccess;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.access.Namespace;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.token.BaseListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.StrutsDelegate;
import com.redhat.rhn.frontend.taglibs.list.helper.ListSessionSetHelper;
import com.redhat.rhn.manager.system.ServerGroupManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author paji
 * ListRemoveGroupsAction
 */
public class ListRemoveGroupsAction extends BaseListAction<ManagedServerGroup> {

    private static final ServerGroupManager SERVER_GROUP_MANAGER = GlobalInstanceHolder.SERVER_GROUP_MANAGER;

    /** {@inheritDoc} */
    @Override
    public ActionForward handleDispatch(ListSessionSetHelper helper,
            ActionMapping mapping,
            ActionForm formIn, HttpServletRequest request,
            HttpServletResponse response) {
        RequestContext context = new RequestContext(request);
        ActivationKey key = context.lookupAndBindActivationKey();
        User user = context.getCurrentUser();
        ensureRoleBasedAccess(user, "systems.activation_keys.groups", Namespace.AccessMode.W);
        Set<String> set = helper.getSet();
        for (String id : set) {
            Long sgid = Long.valueOf(id);
            key.removeServerGroup(SERVER_GROUP_MANAGER.lookup(sgid, user));
        }

        getStrutsDelegate().saveMessage(
                    "activation-key.groups.jsp.removed",
                        new String [] {String.valueOf(set.size())}, request);

        Map<String, Object> params = new HashMap<>();
        params.put(RequestContext.TOKEN_ID, key.getToken().getId().toString());
        StrutsDelegate strutsDelegate = getStrutsDelegate();
        return strutsDelegate.forwardParams
                        (mapping.findForward("success"), params);
    }

    /** {@inheritDoc} */
    @Override
    public List<ManagedServerGroup> getResult(RequestContext context) {
        ActivationKey key = context.lookupAndBindActivationKey();
        List<ManagedServerGroup> groups = key.getServerGroups().stream()
                .filter(g -> g instanceof ManagedServerGroup)
                .map(g -> (ManagedServerGroup)g)
                .collect(Collectors.toList());
        AddGroupsAction.setupAccessMap(context, groups);
        return groups;
    }
}
