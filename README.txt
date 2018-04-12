OVERVIEW
--------------------------------------------------------------------------------
This application is in response to a question utilizing some of the API of Backblaze's 
B2 product. The instructions for this were as follows:

	INSTRUCTIONS
	-------------
	Create a small application using data stored in B2:
    	a) Setup:
    		i) Create a B2 account on the Backblaze website (www.backblaze.com)
    			(1) Note: you do not need to put down a credit card to create a
    			B2 account
    		ii) Sign in to your B2 account
    		iii) Create a B2 Bucket on the Buckets page iv) Add some files to
    		your bucket
    	b) Create a runnable Java application that will list the files in your
    	   bucket and download them to a designated directory.
    		i)  You can use the Java samples provided in the B2 API
    		    documentation
    		ii) Or, you can use the b2-sdk

I included several sections in this document that will provide insight into the
inner workings of the application as well as my decision making while developing
it.


MOTIVATION
--------------------------------------------------------------------------------
As this was an exercise to determine my capabilities as a software engineer, I
decided to build an application just using publicly available HTTP POST and
GET requests as described in https://www.backblaze.com/b2/docs/. I believe this
gave me the best opportunity to demonstrate a wide range of skills. I felt the
best approach was to develop the beginnings of a framework for working with the
B2 API. The application is described in more detail in the APPLICATION OVERVIEW
section.


CAVEATS
--------------------------------------------------------------------------------
There are certain areas of the application that I did not spend too much time
providing polish as my focus was on the core functionality of the application.

	1. The application is heavily documented. This was done not only to help
		make the application clear, but to provide insight into my decisions
		as well as make observations about aspects of the code.

	2. The application was built using Intellij. I used Intellij specific
		features to utilize an Avail-based jar file for supporting JSON
		reading and writing. The interaction of the user to the running program
		takes place in the Intellij console. At times the placement of the
		cursor can have odd behavior. This is a side-effect of using the
		IntelliJ console; I made no attempt to rectify the odd behavior.

	3. So as not to be bogged down in UI details, I decided the best approach
		was to use a console-based application. Because IDE's generally don't
		support running an application directly in a terminal, I created
		ConsoleUtility to adapt the interface to where the application is
		running (terminal vs IDE console). Because providing asynchronous
		notifications via the console would have been clunky, I did utilize
		JOptionPanes to create a popup when a group of files is done downloading
		or when the file fails to download. In order to clear these, you must
		click "OK" in the order received if downloading more than on page of
		files. If too many windows are open, the application will wait until
		you close them. This is wouldn't be a concern with a real implementation
		as these pop-ups wouldn't exist to hang around waiting to be closed as
		a real interfaced would be designed to work with the framework.

	4. I included exception handling throughout the application, opting to
		mostly terminate the running application in the event of failures.
		These exceptions mostly deal with connection, authentication, and
		version control, all things I believe necessitate terminating the
		application due to their potential security implications. Because
		this is handled via continuation-passing, "real" handling of
		managed-exceptions would be easy to implement.

	5. Unit tests - My architecture allowed me to easily create a test client
		which allows for some testing of the application. Ultimately the full
		application cannot be easily tested due to its concurrent-asynchronous
		design. Some of the test assertions are in continuations that run on
		other threads. To get it to fail, I rethrow the exceptions so they
		at least appear in the console though it is claimed the overall test
		passes. I also recently pushed unit-test work for Avail. It is another
		public example of my work building tests. It can be found here:
		https://github.com/AvailLang/Avail/tree/master/test/com/avail/test/utility/json

	6. The application utilizes a properties file for storing account ids and
		authorization keys. This allows for the preservation of this data
		after the application stops running. Running the unit tests will delete
		this file. At startup, if the application does not find the properties
		file in the config folder, it will prompt you for the account data and
		create one.

	7. API with paging results, such as b2_list_file_names, do not have their
		results cached when paging forward via subsequent requests.


APPLICATION OVERVIEW
--------------------------------------------------------------------------------
The application's main entry point is B2Application. The application utilizes a
multi-threaded asynchronous continuation passing architecture with a central
client abstraction to remove the connection implementation (e.g HTTP) and
defined states that indicate the current "state" of the application defining
what can and can't be done at any given time. The application's runtime and
state is managed by ApplicationRuntime.

The application is built around a hierarchy of versioned APIRequests and
corresponding versioned APIResponses. APIResponses are simply vessels that
contain the response data received from the B2 server in response to an
APIRequest. The concrete response implementations are responsible for statically
maintaining versioning information to indicate what versions of the APIRequests
they are compatible with. Otherwise they hold on to state specific to the result
of their corresponding request.

APIRequests is a generic abstraction whose implementations correspond 1-1 with
the Backblaze B2 API. Because of the scope of this project, only four of the API
were implemented. The APIRequests are processed via the AuthenticationContext
(a centralized construct for managing state and authentication/authorization).
All APIRequests should be listed in APICatalogue as this is how interactions
with various clients are identified (e.g. GET vs POST with respect to HTTP).

Because the authorization token expires and the application allows for the
changing of the account being accessed while the application is running,
requests are only allowed to be sent to the B2 API server when the application
is in the correct state for that request; the only two viable states for sending
requests are ACCOUNT_INITIALIZED and AUTHENTICATED. Otherwise, they
are queued to wait in a BlockingQueue until the AuthenticationContext returns
to the proper state (if appropriate). The requests are then made on other
threads as supported by the ThreadPoolExecutor in ApplicationRuntime.
