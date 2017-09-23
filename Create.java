/*
 * ****************************************************************************
 * Copyright VMware, Inc. 2010-2016.  All Rights Reserved.
 * ****************************************************************************
 *
 * This software is made available for use under the terms of the BSD
 * 3-Clause license:
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package com.vmware.general;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.Map;

import static java.lang.System.out;

/**
 * <pre>
 * Create
 *
 * This sample creates managed entity like Host-Standalone Cluster
 * Datacenter, and folder
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * parentname   [required] : specifies the name of the parent folder
 * itemtype     [required] : Type of the object to be added
 *                           e.g. Host-Standalone | Cluster | Datacenter | Folder
 * itemname     [required]   : Name of the item added
 *
 * <b>Command Line:</b>
 * Create a folder named myFolder under root folder Root:
 * run.bat com.vmware.general.Create --url [webserviceurl]
 * --username [username] --password [password]
 * --parentName [Root] --itemType [Folder] --itemName [myFolder]
 *
 * Create a datacenter named myDatacenter under root folder Root:
 * run.bat com.vmware.general.Create --url [webserviceurl]
 * --username [username] --password [password]
 * --parentName [Root] --itemType [Datacenter] --itemName [myDatacenter]
 *
 * Create a cluster named myCluster under root folder Root:
 * run.bat com.vmware.general.Create --url [webserviceurl]
 * --username [username] --password [password]
 * --parentName [Root] --itemType [Cluster] --itemName [myCluster]
 * </pre>
 */

@Sample(
        name = "create",
        description = "This sample creates managed entity like Host-Standalone," +
                " Cluster, Datacenter, and folder"
)
public class Create extends ConnectedVimServiceBase {
    private String licenseKey;
    private String parentName;
    private String itemType;
    private String itemName;

    @Option(name = "parentname", description = "specifies the name of the parent folder")
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    @Option(name = "itemtype", description = "Type of the object to be added, e.g. Host-Standalone | Cluster | Datacenter | Folder")
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @Option(name = "itemname", description = "Name of the item added")
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Option(name = "licensekey", required = false)
    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    @Action
    public void create() throws DuplicateNameFaultMsg,
            InvalidNameFaultMsg, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, HostConnectFaultFaultMsg, InvalidLoginFaultMsg, InvalidCollectorVersionFaultMsg {

        ManagedObjectReference taskMoRef = null;
        Map<String, ManagedObjectReference> folders =
                getMOREFs.inFolderByType(serviceContent.getRootFolder(), "Folder");
        if (folders.containsKey(parentName)) {
            ManagedObjectReference folderMoRef = folders.get(parentName);
            if (itemType.equals("Folder")) {
                vimPort.createFolder(folderMoRef, itemName);
                out.println("Sucessfully created::" + itemName);
            } else if (itemType.equals("Datacenter")) {
                vimPort.createDatacenter(folderMoRef, itemName);
                out.println("Sucessfully created::" + itemName);
            } else if (itemType.equals("Cluster")) {
                ClusterConfigSpec clusterSpec = new ClusterConfigSpec();
                vimPort.createCluster(folderMoRef, itemName, clusterSpec);
                out.println("Sucessfully created::" + itemName);
            } else if (itemType.equals("Host-Standalone")) {
                HostConnectSpec hostSpec = new HostConnectSpec();
                hostSpec.setHostName(itemName);
                hostSpec.setUserName(connection.getUsername());
                hostSpec.setPassword(connection.getPassword());
                hostSpec.setPort(connection.getPort());
                ComputeResourceConfigSpec crcs = new ComputeResourceConfigSpec();
                crcs.setVmSwapPlacement(VirtualMachineConfigInfoSwapPlacementType.VM_DIRECTORY
                        .value());
                taskMoRef =
                        vimPort.addStandaloneHostTask(folderMoRef, hostSpec, crcs,
                                true, this.licenseKey);

                if (getTaskResultAfterDone(taskMoRef)) {
                    out.println("Sucessfully created::" + itemName);
                } else {
                    out.println("Host'" + itemName + " not created::");
                }
            } else {
                out.println("Unknown Type. Allowed types are:");
                out.println(" Host-Standalone");
                out.println(" Cluster");
                out.println(" Datacenter");
                out.println(" Folder");
            }
        } else {
            out.println("Parent folder '" + parentName + "' not found");
        }

    }

    /**
     * This method returns a boolean value specifying whether the Task is
     * succeeded or failed.
     *
     * @param task ManagedObjectReference representing the Task.
     * @return boolean value representing the Task result.
     * @throws InvalidCollectorVersionFaultMsg
     *
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    boolean getTaskResultAfterDone(ManagedObjectReference task)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg,
            InvalidCollectorVersionFaultMsg {

        boolean retVal = false;

        // info has a property - state for state of the task
        Object[] result =
                waitForValues.wait(task, new String[]{"info.state", "info.error"},
                        new String[]{"state"}, new Object[][]{new Object[]{
                        TaskInfoState.SUCCESS, TaskInfoState.ERROR}});

        if (result[0].equals(TaskInfoState.SUCCESS)) {
            retVal = true;
        }
        if (result[1] instanceof LocalizedMethodFault) {
            throw new RuntimeException(
                    ((LocalizedMethodFault) result[1]).getLocalizedMessage());
        }
        return retVal;
    }
}