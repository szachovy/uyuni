/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.channel.manage;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelVersion;
import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.domain.channel.SelectableChannel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.frontend.struts.RhnListSetHelper;
import com.redhat.rhn.frontend.taglibs.list.ListTagHelper;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * AddRedHatErrataAction
 */
public class AddRedHatErrataAction extends RhnListAction {


    private static final String CID = "cid";
    private static final String SELECTED_CHANNEL = "selected_channel";
    private static final String SELECTED_CHANNEL_OLD = "selected_channel_old";

    private static final String CHANNEL_LIST = "channel_list";

    private static final String VERSION_SUBMIT = "frontend.actions.channels.manager." +
            "add.viewChannels";
    private static final String CHANNEL_SUBMIT = "frontend.actions.channels.manager." +
            "add.viewErrata";
    private static final String CHECKED = "assoc_checked";
    private static final String LIST_ALREADY_INCLUDED_PATCH = "list_already_included";

    private static final String SUBMITTED = "frontend.actions.channels.manager." +
            "add.submit";
    private static final String EMPTY_KEY = "channel.manage.errata.noerrata";

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);
        User user =  requestContext.getCurrentUser();
        Long cid = Long.parseLong(request.getParameter(CID));
        Channel currentChan = ChannelFactory.lookupByIdAndUser(cid, user);
        Channel selectedChannel = null;


        ErrataHelper.checkPermissions(user, cid);

        request.setAttribute(CID, cid);
        request.setAttribute("user", user);
        request.setAttribute("channel_name", currentChan.getName());
        request.setAttribute(ListTagHelper.PARENT_URL, request.getRequestURI() + "?" + request.getQueryString());
        request.setAttribute("emptyKey", EMPTY_KEY);

        List<SelectableChannel> channelList;
        String selectedChannelStr;
        boolean checked = true;
        boolean listAlreadyIncludedPatches = true;


        //Set initail strings
        selectedChannelStr = request.getParameter(SELECTED_CHANNEL_OLD);

        //If the channel submit button was clicked
        if (requestContext.wasDispatched(CHANNEL_SUBMIT)) {
          selectedChannelStr = request.getParameter(SELECTED_CHANNEL);
          // selectedChannelStr might be null
        }

        //If this is a clone, go ahead and pre-select the original Channel
        Channel original = ChannelFactory.lookupOriginalChannel(currentChan);
        if (!requestContext.isSubmitted() && original != null) {
            selectedChannel = original;
            selectedChannelStr = selectedChannel.getId().toString();
        }

        // show all channels in the list
        List<Channel> channelSet = ChannelFactory.listRedHatBaseChannels();
        channelList = new ArrayList<>();
        if (channelSet != null) {
            sortChannelsAndChildify(channelSet, channelList, user, selectedChannelStr);
            request.setAttribute(CHANNEL_LIST, channelList);
        }
        if (!channelList.isEmpty() && selectedChannelStr == null) {
            selectedChannelStr = channelList.get(0).getId().toString();
        }

        if (requestContext.isSubmitted() && request.getParameter(CHECKED) == null)  {
            checked = false;
        }

        if (requestContext.isSubmitted() && request.getParameter(LIST_ALREADY_INCLUDED_PATCH) == null)  {
            listAlreadyIncludedPatches = false;
        }

        request.setAttribute(CHECKED, checked);
        request.setAttribute(SELECTED_CHANNEL, selectedChannelStr);

        if (requestContext.wasDispatched(SUBMITTED)) {
            Map<String, Object> params = new HashMap<>();
            params.put(CID, request.getParameter(CID));
            params.put(SELECTED_CHANNEL, selectedChannelStr);
            params.put(CHECKED, request.getParameter(CHECKED));
            return getStrutsDelegate().forwardParams(mapping.findForward("submit"), params);
        }

        //If we clicked on the channel selection, clear the set
        if (requestContext.wasDispatched(CHANNEL_SUBMIT) ||
                requestContext.wasDispatched(VERSION_SUBMIT) ||
                !requestContext.isSubmitted()) {
            RhnSet eset =  getSetDecl(currentChan).get(user);
            eset.clear();
            RhnSetManager.store(eset);
        }

        if (selectedChannelStr !=  null) {
            selectedChannel = ChannelFactory.lookupByIdAndUser(Long.parseLong(selectedChannelStr), user);
        }

        RhnListSetHelper helper = new RhnListSetHelper(request);
        RhnSet set =  getSetDecl(currentChan).get(user);

        DataResult<ErrataOverview> dr = getData(request, selectedChannel, currentChan, channelList, checked,
                listAlreadyIncludedPatches, user);

        request.setAttribute(RequestContext.PAGE_LIST, dr);

        if (ListTagHelper.getListAction("errata", request) != null) {
            helper.execute(set, "errata", dr);
        }
        if (!set.isEmpty()) {
            helper.syncSelections(set, dr);
            ListTagHelper.setSelectedAmount("errata", set.size(), request);
        }

        TagHelper.bindElaboratorTo("errata", dr.getElaborator(), request);
        ListTagHelper.bindSetDeclTo("errata", getSetDecl(currentChan), request);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }


    private void sortChannelsAndChildify(List<Channel> listToSort,
            List<SelectableChannel> resultList,
            User user, String selectedId) {

        Long cid = null;
        if (selectedId != null) {
            cid = Long.parseLong(selectedId);
        }

        Collections.sort(listToSort);
        for (Channel chan : listToSort) {
            List<Channel> children = chan.getAccessibleChildrenFor(user);
            Collections.sort(children);
            resultList.add(new SelectableChannel(chan));
            if (cid != null && chan.getId().equals(cid)) {
                resultList.get(resultList.size() - 1).setSelected(true);
            }
            for (Channel childChan : children)  {
                resultList.add(new SelectableChannel(childChan));
                if (cid != null && childChan.getId().equals(cid)) {
                    resultList.get(resultList.size() - 1).setSelected(true);
                }
            }
        }
    }


    private String findVersionFromChannel(Channel channel) {
        for (DistChannelMap map : channel.getDistChannelMaps()) {
            ChannelVersion ver  =  ChannelVersion.getChannelVersionForDistChannelMap(map);
            if (ver != null) {
                return ver.getVersion();
            }
        }
        return null;
    }


    private List<Channel> findChannelsByVersion(User user, String version) {

        if (version == null) {
            return null;
        }
        List<Channel> channels = ChannelFactory.listRedHatBaseChannels(user);
        List<Channel> toReturn = new ArrayList<>();
        for (Channel chan : channels) {
            for (DistChannelMap map : chan.getDistChannelMaps()) {
                if (ChannelVersion.getChannelVersionForDistChannelMap(map).
                        getVersion().equals(version)) {
                   toReturn.add(chan);
                   break;
                }
            }
        }
        return toReturn;
    }


    protected RhnSetDecl getSetDecl(Channel chan) {
        return RhnSetDecl.setForChannelErrata(chan);
    }


    private DataResult<ErrataOverview> getData(HttpServletRequest request,
                                               Channel selectedChan,
                                               Channel currentChan,
                                               List<SelectableChannel> selChannelList,
                                               boolean packageAssoc,
                                               boolean listAlreadyIncludedPatches,
                                               User user) {

        if (selectedChan != null) {
            RhnSet set = RhnSetDecl.CHANNELS_FOR_ERRATA.get(user);
            set.clear();
            set.addElement(selectedChan.getId());
            RhnSetManager.store(set);
            return ChannelManager.findErrataFromRhnSetForTarget(currentChan,
                    packageAssoc, listAlreadyIncludedPatches, user);
        }
        else if (selChannelList != null) {
                RhnSet set = RhnSetDecl.CHANNELS_FOR_ERRATA.get(user);
                set.clear();
                for (SelectableChannel chan : selChannelList) {
                    set.addElement(chan.getId());
                }
                RhnSetManager.store(set);
                return ChannelManager.findErrataFromRhnSetForTarget(currentChan,
                        packageAssoc, listAlreadyIncludedPatches, user);

        }
        else {
                return ChannelManager.findErrataForTarget(currentChan, packageAssoc);
        }
    }

}
