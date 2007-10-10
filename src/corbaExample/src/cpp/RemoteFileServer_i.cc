/*
Copyright (c) 2007 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

#include <sstream>
#include <iostream>
#include <fstream>
#include <RemoteFileServer.hh>

//
// Simple example c++ corba server which can be the target of a streamed file.
//
class examples_iiop_RemoteFileServer_i: public POA_examples::iiop::RemoteFileServer {
private:
  int _nextId;
  // Make sure all instances are built on the heap by making the
  // destructor non-public
  //virtual ~examples_iiop_RemoteFileServer_i();
public:
  // standard constructor
  examples_iiop_RemoteFileServer_i();
  virtual ~examples_iiop_RemoteFileServer_i();

  // methods corresponding to defined IDL attributes and operations
  void sendFile(com::healthmarketscience::rmiio::RemoteInputStream_ptr arg0);

};

examples_iiop_RemoteFileServer_i::examples_iiop_RemoteFileServer_i()
  : _nextId(0)
{
}
examples_iiop_RemoteFileServer_i::~examples_iiop_RemoteFileServer_i(){
}
void examples_iiop_RemoteFileServer_i::sendFile(com::healthmarketscience::rmiio::RemoteInputStream_ptr remoteStream){

  std::ostringstream oss;
  oss << "/tmp/file_" << _nextId++ << ".tmp";
  std::string fname = oss.str();
  std::cout << "Writing file " << fname << std::endl;
  std::fstream fs(fname.c_str(), std::ios::out | std::ios::binary);
  if(!fs.is_open()) {
    std::cerr << "failed opening " << fname << std::endl;
  }

  long nextPacketId = 0;
  bool finished = false;
  while(!finished) {

    org::omg::boxedRMI::seq1_octet* packetPtr = remoteStream->readPacket(
        nextPacketId++);
    org::omg::boxedRMI::seq1_octet_var packet = packetPtr;
    if(packetPtr) {
      const unsigned char* data = &((*packet)[0]);
      fs.write((const char*)data, packet->length());
    } else {
      finished = true;
    }
  }
  
  fs.flush();
  std::cout << "Finished writing file " << fname << std::endl;
  
  fs.close();
  remoteStream->close(true);
}




int main(int argc, char** argv)
{
  try {
    // Initialise the ORB.
    CORBA::ORB_var orb = CORBA::ORB_init(argc, argv);

    // Obtain a reference to the root POA.
    CORBA::Object_var obj = orb->resolve_initial_references("RootPOA");
    PortableServer::POA_var poa = PortableServer::POA::_narrow(obj);

    // We allocate the objects on the heap.  Since these are reference
    // counted objects, they will be deleted by the POA when they are no
    // longer needed.
    examples_iiop_RemoteFileServer_i* myexamples_iiop_RemoteFileServer_i = new examples_iiop_RemoteFileServer_i();


    // Activate the objects.  This tells the POA that the objects are
    // ready to accept requests.
    PortableServer::ObjectId_var myexamples_iiop_RemoteFileServer_iid = poa->activate_object(myexamples_iiop_RemoteFileServer_i);


    // Obtain a reference to each object and output the stringified
    // IOR to stdout
    {
      // IDL interface: examples::iiop::RemoteFileServer
      CORBA::Object_var ref = myexamples_iiop_RemoteFileServer_i->_this();
      CORBA::String_var sior(orb->object_to_string(ref));
      std::cout << "IDL object examples::iiop::RemoteFileServer IOR = '" << (char*)sior << "'" << std::endl;

      CORBA::Object_var obj = orb->resolve_initial_references("NameService");
      // Narrow the reference returned.
      CosNaming::NamingContext_var rootContext =
        CosNaming::NamingContext::_narrow(obj);
      if( CORBA::is_nil(rootContext) ) {
        std::cerr << "Failed to narrow the root naming context." << std::endl;
        return 0;
      }

       // Bind objref with name "RemoteFileServer" to the root naming context
      CosNaming::Name objectName;
      objectName.length(1);
      objectName[0].id = (const char*) "RemoteFileServer"; // string copied

      rootContext->rebind(objectName, ref);
    }

    // Obtain a POAManager, and tell the POA to start accepting
    // requests on its objects.
    PortableServer::POAManager_var pman = poa->the_POAManager();
    pman->activate();

    orb->run();
    orb->destroy();
  }
  catch(CORBA::TRANSIENT&) {
    std::cerr << "Caught system exception TRANSIENT -- unable to contact the "
         << "server." << std::endl;
  }
  catch(CORBA::SystemException& ex) {
    std::cerr << "Caught a CORBA::" << ex._name() << std::endl;
  }
  catch(CORBA::Exception& ex) {
    std::cerr << "Caught CORBA::Exception: " << ex._name() << std::endl;
  }
  catch(omniORB::fatalException& fe) {
    std::cerr << "Caught omniORB::fatalException:" << std::endl;
    std::cerr << "  file: " << fe.file() << std::endl;
    std::cerr << "  line: " << fe.line() << std::endl;
    std::cerr << "  mesg: " << fe.errmsg() << std::endl;
  }
  return 0;
}

