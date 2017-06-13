/**
 * @license
 * Copyright 2017 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package foam.nanos.boot;

import foam.core.*;
import foam.dao.*;
import foam.mlang.*;
import foam.nanos.auth.*;
import foam.nanos.pm.*;
import foam.nanos.pm.PMDAO;

public class Boot {
  protected DAO    serviceDAO_;
  protected DAO    userDAO_;
  protected DAO    groupDAO_;
  protected DAO    pmDAO_;
  protected X         root_ = new ProxyX();

  public Boot() {
    // Used for all the services that will be required when Booting
    MapDAO serviceDAOConstruct = new MapDAO();
    serviceDAOConstruct.setOf(NSpec.getOwnClassInfo());
    serviceDAOConstruct.setX(root_);
    serviceDAO_ = serviceDAOConstruct;

    // Used to hold all of the users in our system
    MapDAO userDAOConstruct = new MapDAO();
    userDAOConstruct.setOf(User.getOwnClassInfo());
    userDAOConstruct.setX(root_);
    userDAO_ = userDAOConstruct;
    root_.put("userDAO", userDAO_);

    // Used for groups. We have multiple groups that contain different users
    MapDAO groupDAOConstruct = new MapDAO();
    groupDAOConstruct.setOf(Group.getOwnClassInfo());
    groupDAOConstruct.setX(root_);
    groupDAO_ = groupDAOConstruct;
    root_.put("groupDAO", groupDAO_);

    loadServices();

    serviceDAO_.select(new AbstractSink() {
      public void put(FObject obj, Detachable sub) {
        NSpec sp = (NSpec) obj;
        System.out.println("Registering: " + sp.getName());
        root_.putFactory(sp.getName(), new SingletonFactory(new NSpecFactory(sp)));
      }
    });

    /**
     * Revert root_ to non ProxyX to avoid letting children add new bindings.
     */
    root_ = root_.put("firewall", "firewall");

    serviceDAO_.where(foam.mlang.MLang.EQ(NSpec.LAZY, false)).select(new AbstractSink() {
      public void put(FObject obj, Detachable sub) {
        NSpec sp = (NSpec) obj;

        System.out.println("Starting: " + sp.getName());
        root_.get(sp.getName());
      }
    });
  }

  protected void loadServices() {
    NSpec s = new NSpec();
    s.setName("http");
    s.setServiceClass("foam.nanos.http.NanoHttpServer");
    s.setLazy(false);
    serviceDAO_.put(s);

    NSpec dpl = new NSpec();
    dpl.setName(DAOPMLogger.ServiceName);
    dpl.setServiceClass(DAOPMLogger.class.getName());
    serviceDAO_.put(dpl);

    NSpec pmd = new NSpec();
    pmd.setName(PMDAO.ServiceName); // pmInfoDAO
    pmd.setServiceClass(PMDAO.class.getName());
    pmd.setServe(true);
    serviceDAO_.put(pmd);

    NSpec authTest = new NSpec();
    authTest.setName("authTest");
    authTest.setServiceClass("foam.nanos.auth.UserAndGroupAuthServiceTest");
    // authTest.setLazy(false);
    serviceDAO_.put(authTest);

    NSpec logger = new NSpec();
    logger.setName("logger");
    logger.setServiceClass("foam.nanos.logger.NanoLogger");
    serviceDAO_.put(logger);

    NSpec ping = new NSpec();
    ping.setName("ping");
    ping.setServiceClass("foam.nanos.http.PingService");
    serviceDAO_.put(ping);

    NSpec uptime = new NSpec();
    uptime.setName("uptime");
    uptime.setServiceClass("foam.nanos.http.UptimeServlet");
    uptime.setLazy(false);
    serviceDAO_.put(uptime);
  }

  public static void main (String[] args)
    throws Exception
  {
    System.out.println("Starting Nanos Server");
    new Boot();
  }
}
