/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.quickstarts.xa.server;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.TransactionSynchronizationRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

/**
 * @author Stuart Douglas
 * @author Ivo Studensky
 */
@Remote(StatefulRemote.class)
@Stateful
public class StatefulBean implements SessionSynchronization, StatefulRemote {

    private Boolean commitSucceeded;
    private boolean beforeCompletion = false;
    private Object transactionKey = null;
    private boolean rollbackOnlyBeforeCompletion = false;

    @Resource
    private SessionContext sessionContext;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;


    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public int transactionStatus() {
        System.out.printf("StatefulBean:transactionStatus%n");
        return transactionSynchronizationRegistry.getTransactionStatus();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void resetStatus() {
        System.out.printf("StatefulBean:resetStatus%n");
        commitSucceeded = null;
        beforeCompletion = false;
        transactionKey = null;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void setRollbackOnlyBeforeCompletion(boolean rollbackOnlyBeforeCompletion) throws RemoteException {
        System.out.printf("StatefulBean:setRollbackOnlyBeforeCompletion%n");
        this.rollbackOnlyBeforeCompletion = rollbackOnlyBeforeCompletion;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String sameTransaction(boolean first) throws RemoteException {
        System.out.printf("StatefulBean:sameTransaction%n");
        if (first) {
            transactionKey = transactionSynchronizationRegistry.getTransactionKey();

            return "StatefulBean:sameTransaction: transactionKey = " + transactionKey;
        } else if (!transactionKey.equals(transactionSynchronizationRegistry.getTransactionKey())) {
            throw new RemoteException("Transaction on second call was not the same as on first call");
        }

        return "StatefulBean:sameTransaction: transactionKey is the same: " + transactionKey;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void rollbackOnly() throws RemoteException {
        System.out.printf("StatefulBean:rollbackOnly%n");
        this.sessionContext.setRollbackOnly();
    }

    public void ejbCreate() {

    }

    public void afterBegin() throws EJBException, RemoteException {

    }

    public void beforeCompletion() throws EJBException, RemoteException {
        System.out.printf("StatefulBean:beforeCompletion%n");
        beforeCompletion = true;

        if (rollbackOnlyBeforeCompletion) {
            this.sessionContext.setRollbackOnly();
        }
    }

    public void afterCompletion(final boolean committed) throws EJBException, RemoteException {
        System.out.printf("StatefulBean:afterCompletion%n");
        commitSucceeded = committed;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Boolean getCommitSucceeded() {
        return commitSucceeded;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isBeforeCompletion() {
        return beforeCompletion;
    }

    @Override
    public String injectFault(String faultType) {
        if (faultType.contains("HALT")) {
            Runtime.getRuntime().halt(1);
        }

        try {
            return "fault injected on host " + InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return e.getMessage();
        }
    }
}
