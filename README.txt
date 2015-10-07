RMIIO

RMIIO is a library which makes it as simple as possible to stream large
amounts of data using the RMI framework (or any RPC framework for that
matter). Who needs this? Well, if you have ever needed to send a file from an
RMI client to an RMI server, you have faced this problem. And, if you did
manage to implement a basic solution, it probably threw an OutOfMemoryError
the first time someone tried to send a 2GB file. Due to the design of RMI,
this common and deceptively simple problem is actually quite difficult to
solve in an efficient and robust manner. 

The RMI framework makes it very easy to implement remote communication between
java programs. It takes a very difficult problem (remote communication) and
presents a fairly easy to use solution. However, the RMI framework is designed
around sending and receiving groups of objects which are all immediately
available in memory. How do you send a file from the client to the server
without blowing out memory on the client or the server? The tools and APIs in
the standard java runtime do not have any ready solutions to this problem, yet
many people have encountered it. 

What you really want to do is stream data from the client to the server (you
have an InputStream, right?) using a framework which does not really expose a
streaming model. The RMIIO library was written to fill in that missing gap in
the RMI framework. It provides some very powerful classes which enable a
client to stream data to the server using only a few extra lines of code.


Please note that the GitHub repository is a mirror of the main project
repository which is hosted on SourceForge:

Homepage: http://openhms.sourceforge.net/rmiio/

Project: https://sourceforge.net/projects/openhms/

