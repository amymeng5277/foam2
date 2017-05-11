/**
 * @license
 * Copyright 2017 The FOAM Authors. All Rights Reserved.
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package foam.nanos.boot;

import foam.core.*;
import foam.dao.*;
import foam.nanos.*;

public class Boot {

  protected DAO serviceDAO_;
  protected X   root_ = new ProxyX();

  public Boot() {
    serviceDAO_ = new MapDAO();
    loadTestData();
    ((MapDAO) serviceDAO_).setOf(NSpec.getOwnClassInfo());

    ((AbstractDAO) serviceDAO_).select(new AbstractSink() {
      public void put(FObject obj, Detachable sub) {
        NSpec sp = (NSpec) obj;
        System.out.println("NSpec: " + sp.getName());

        try {
          NanoService ns = sp.createService();

          ((ContextAwareSupport) ns).setX(root_);
          ns.start();
          root_.put(sp.getName(), ns);
        } catch (ClassNotFoundException e) {
           e.printStackTrace();
        } catch (InstantiationException e) {
           e.printStackTrace();
        } catch (IllegalAccessException e) {
           e.printStackTrace();
        }
      }
    });
  }

  protected void loadTestData() {
    NSpec s = new NSpec();
    s.setName("http");
    s.setServiceClass("foam.nano.http.HttpServer");
    serviceDAO_.put(s);
  }

  public void main (String[] args) throws Exception {
    new Boot();

    Thread.currentThread().setDaemon(true);
  }

}