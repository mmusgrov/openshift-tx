package org.jboss.as.quickstarts.xa.client;

import org.jboss.as.quickstarts.xa.server.StatefulRemote;
import org.jboss.as.quickstarts.xa.server.StatelessRemote;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Hashtable;

@Stateless
@Remote (TransactionalLocal.class)
public class TransactionalLocalBean implements TransactionalLocal {
    @Resource
    private UserTransaction userTransaction;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private StatelessRemote statelessRemote;

    private StatefulRemote statefulEJB;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String transactionStatus() {
            String message;

            try {
                StatelessRemote bean = getTransactionalBean(
                        "StatelessBean", StatelessRemote.class.getCanonicalName());

                assert Status.STATUS_NO_TRANSACTION == bean.transactionStatus() : "No transaction expected!";
                userTransaction.begin();
                try {
                    assert Status.STATUS_ACTIVE == bean.transactionStatus() : "Active transaction expected!";
                } finally {
                    userTransaction.rollback();
                    message = "success";
                }
            } catch (RemoteException | NotSupportedException | SystemException | IllegalStateException |
                    SecurityException | NamingException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                message = sw.toString();
            }

            return message;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String testSameTransactionEachCall() {
        String message;

        try {
            StatefulRemote bean = getTransactionalStatefulBean(
                    "StatefulBean", StatefulRemote.class.getCanonicalName());

            userTransaction.begin();
            try {
                String k1 = bean.sameTransaction(true);
                System.out.printf("client first key = %s%n", k1);
                String k2 = bean.sameTransaction(false);
                System.out.printf("client second key = %s%n", k2);
            } finally {
                userTransaction.rollback();
                message = "success";
            }
        } catch (NotSupportedException | SystemException | RemoteException | IllegalStateException | SecurityException
                | NamingException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            message = sw.toString();
        }

        return message;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String injectFault(String arg) {
        String message;
        boolean inTxn = arg.contains("notx");

        try {
            StatefulRemote bean = getTransactionalStatefulBean(
                    "StatefulBean", StatefulRemote.class.getCanonicalName());

            if (inTxn) {
                userTransaction.begin();
            }

            try {
                String k1 = bean.injectFault(arg);
                System.out.printf("client injectFault: %s%n", k1);
            } finally {
                if (inTxn) {
                    userTransaction.rollback();
                }

                message = "success";
            }
        } catch (NotSupportedException | SystemException | RemoteException | IllegalStateException | SecurityException
                | NamingException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            message = sw.toString();
        }

        return message;    }

    private StatelessRemote getTransactionalBean(String beanName, String viewClassName) throws NamingException {
        if (statelessRemote == null)
            statelessRemote = (StatelessRemote) lookupBean(beanName, viewClassName, false);

        return statelessRemote;
    }

    private StatefulRemote getTransactionalStatefulBean(String beanName, String viewClassName) throws NamingException {
        if (statefulEJB == null)
            statefulEJB = (StatefulRemote) lookupBean(beanName, viewClassName, true);

        return statefulEJB;
    }

    private Object lookupBean(String beanName, String viewClassName, boolean staeful) throws NamingException {
        Hashtable properties = new Hashtable();
        properties.put(javax.naming.Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        javax.naming.Context jndiContext = new javax.naming.InitialContext(properties);
        // context.lookup("ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName + "?stateful");
        String jndiName = String.format("ejb:/tx-server//%s!%s", beanName, viewClassName);
        if (staeful)
            jndiName += "?stateful";
        System.out.printf("looking up bean name %s%n", jndiName);
        return jndiContext.lookup(jndiName);
    }

    public static String stringForm(int status) {
        switch (status) {
            case javax.transaction.Status.STATUS_ACTIVE:
                return "javax.transaction.Status.STATUS_ACTIVE";
            case javax.transaction.Status.STATUS_COMMITTED:
                return "javax.transaction.Status.STATUS_COMMITTED";
            case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
                return "javax.transaction.Status.STATUS_MARKED_ROLLBACK";
            case javax.transaction.Status.STATUS_NO_TRANSACTION:
                return "javax.transaction.Status.STATUS_NO_TRANSACTION";
            case javax.transaction.Status.STATUS_PREPARED:
                return "javax.transaction.Status.STATUS_PREPARED";
            case javax.transaction.Status.STATUS_PREPARING:
                return "javax.transaction.Status.STATUS_PREPARING";
            case javax.transaction.Status.STATUS_ROLLEDBACK:
                return "javax.transaction.Status.STATUS_ROLLEDBACK";
            case javax.transaction.Status.STATUS_ROLLING_BACK:
                return "javax.transaction.Status.STATUS_ROLLING_BACK";
            case javax.transaction.Status.STATUS_UNKNOWN:
            default:
                return "javax.transaction.Status.STATUS_UNKNOWN";
        }
    }
}
