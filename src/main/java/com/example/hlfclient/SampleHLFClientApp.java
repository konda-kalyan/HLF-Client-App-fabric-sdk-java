package com.example.hlfclient;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletionException;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse.Status;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.OrgInfo;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
//import org.hyperledger.fabric_ca.sdk.helper.Util;
//import org.hyperledger.fabric.gateway.Wallets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class SampleHLFClientApp {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleHLFClientApp.class, args);
		
		NetworkConfig networkConfig = NetworkConfig.fromYamlFile(new File("./network-config-tls.yaml"));
		
		OrgInfo clientOrg = networkConfig.getClientOrganization();
		NetworkConfig.CAInfo clientOrgCAInfo = clientOrg.getCertificateAuthorities().get(0);
		
		HFCAClient hfcaClient = HFCAClient.createNewInstance(clientOrgCAInfo.getCAName(), clientOrgCAInfo.getUrl(), clientOrgCAInfo.getProperties());
		
		CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		hfcaClient.setCryptoSuite(cryptoSuite);
		
		/*
		NetworkConfig.UserInfo adminUserContext = clientOrg.getPeerAdmin();
		System.out.println("adminUserContext.getAccount(): " + adminUserContext.getAccount());
		System.out.println("adminUserContext.getName(): " + adminUserContext.getName());
		System.out.println("adminUserContext.getMspId(): " + adminUserContext.getMspId());
		System.out.println("adminUserContext.getAffiliation(): " + adminUserContext.getAffiliation());
		*/
		
		// getting Client Org CA Registrars info (admin info)
		NetworkConfig.UserInfo registrarInfo = clientOrg.getCertificateAuthorities().get(0).getRegistrars().iterator().next();
//		System.out.println("registrarInfo.getAccount(): " + registrarInfo.getAccount()); // somehow it is null
//		System.out.println("registrarInfo.getName(): " + registrarInfo.getName()); // admin
//		System.out.println("registrarInfo.getMspId(): " + registrarInfo.getMspId());
//		System.out.println("registrarInfo.getAffiliation(): " + registrarInfo.getAffiliation()); // somehow it is null, though it is set in network-config yaml file
//		System.out.println("registrarInfo.getEnrollSecret(): " + registrarInfo.getEnrollSecret()); // adminpw
		//System.out.println("registrarInfo.getEnrollment(): " + registrarInfo.getEnrollment().getCert()); // here, NullPointerException
		String afficiation = "org1.department1";
		registrarInfo.setAffiliation("org1.department1");
		Enrollment adminEnrollment = hfcaClient.enroll(registrarInfo.getName(), registrarInfo.getEnrollSecret()); //pass admin username and password
		registrarInfo.setEnrollment(adminEnrollment);
//		System.out.println("registrarInfo.getEnrollment(): " + registrarInfo.getEnrollment().getCert()); // now, it is fine
		
		UserContext user1 = new UserContext();
		Date date = new Date();
	    //This method returns the time in millis
	    long currentTimeInMilliSecs = date.getTime();
	    //System.out.println("Time in milliseconds using Date class: " + currentTimeInMilliSecs);
	    String uniqueUserName = "User" + currentTimeInMilliSecs;
	    //System.out.println("uniqueUserName: " + uniqueUserName);
		user1.setName(uniqueUserName);
		user1.setMspId(clientOrg.getMspId());
		user1.setAffiliation(afficiation);
		Set<String> roles = new HashSet<String>();
		roles.add("client");
		user1.setRoles(roles);
		
		RegistrationRequest rr = new RegistrationRequest(user1.getName(), user1.getAffiliation());
		String enrollmentSecret = hfcaClient.register(rr, registrarInfo);
		//user1.setEnrollSecret(enrollmentSecret);

		Enrollment userEnrollment = hfcaClient.enroll(user1.getName(), enrollmentSecret);
		user1.setEnrollment(userEnrollment);
		//Util.writeUserContext(userContext);
		
//		Collection<HFCAIdentity> identities = hfcaClient.getHFCAIdentities(registrarInfo);
//		for(HFCAIdentity identity : identities) {
//			System.out.println("identity.getEnrollmentId(): " + identity.getEnrollmentId());
//		}
		
		//UserContext adminUserContext1 = Util.readUserContext("org1", "admin");
		//CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		HFClient hfClient = HFClient.createNewInstance();
		hfClient.setCryptoSuite(cryptoSuite);
		hfClient.setUserContext(user1);
		
		// get peer0.org1 info of client org
		String peer0Org1Name = "peer0.org1.example.com";
		String peer0Org1Url = "grpcs://localhost:7051";
		Properties peer0Org1Props = networkConfig.getPeerProperties(peer0Org1Name);
		//String peer0Org1Url = networkConfig.getPeerUrl("peer0Org1Name");
		
		// add peer0.org1 to the client
		Peer peer0Org1 = hfClient.newPeer(peer0Org1Name, peer0Org1Url, peer0Org1Props);
		
		// get peer0.org2 info of client org
		String peer0Org2Name = "peer0.org2.example.com";
		String peer0Org2Url = "grpcs://localhost:9051";
		Properties peer0Org2Props = networkConfig.getPeerProperties(peer0Org2Name);
		//String peer0Org1Url = networkConfig.getPeerUrl("peer0Org1Name");
		
		// add peer0.org1 to the client
		Peer peer1Org1 = hfClient.newPeer(peer0Org2Name, peer0Org2Url, peer0Org2Props);
				
		// add peer0Org1 event hub
		String peer0Org1everntUrl = "grpcs://localhost:7053"; // ensure that port is of event hub		
		EventHub peer0Org1eventHub = hfClient.newEventHub(peer0Org1Name, peer0Org1everntUrl, networkConfig.getEventHubsProperties(peer0Org1Name)); // note that eventhub name and peer names are same and hence passing peer name as eventhub name.
		
		// add peer0Org2 event hub
		String peer0Org2everntUrl = "grpcs://localhost:9053"; // ensure that port is of event hub		
		EventHub peer0Org2eventHub = hfClient.newEventHub(peer0Org2Name, peer0Org2everntUrl, networkConfig.getEventHubsProperties(peer0Org2Name));
		
		// add orderer now
		String ordererName = networkConfig.getOrdererNames().iterator().next();
		String ordererUrl = "grpcs://localhost:7050";
		Orderer orderer = hfClient.newOrderer(ordererName, ordererUrl, networkConfig.getOrdererProperties(ordererName));
		
		Channel channel = hfClient.newChannel("mychannel");
		
		 channel.addPeer(peer0Org1);
		 channel.addPeer(peer1Org1);
		 channel.addEventHub(peer0Org1eventHub);
		 channel.addEventHub(peer0Org2eventHub);
		 channel.addOrderer(orderer);
		 channel.initialize();
		 
		 System.out.println("--- Before newTransactionProposalRequest ----");
		 TransactionProposalRequest request = hfClient.newTransactionProposalRequest();
		 //String cc = "fabcarcc"; // Chaincode name
		 String chaincodeName = "mycc"; // Chaincode name
		 ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).build();
		
		 request.setChaincodeID(chaincodeID);
		 //request.setFcn("initLedger"); // Chaincode invoke funtion name
		 request.setFcn("query"); // Chaincode invoke funtion name
		 //String[] arguments = { "CAR11", "VgW", "Poglo", "Ggrey", "Margy" }; // Arguments that Chaincode function takes
		 String[] arguments = { "a" };  // Arguments that Chaincode function takes
		 request.setArgs(arguments);
		 request.setProposalWaitTime(3000);
		 System.out.println("--- Before sendTransactionProposal ----");
		 Collection<ProposalResponse> responses = channel.sendTransactionProposal(request);
		 
		 for (ProposalResponse res : responses) {
		   // Process response from transaction proposal
			 if (res.getStatus() == ProposalResponse.Status.SUCCESS) {
                 System.out.printf("Successful transaction proposal response Txid: %s from peer %s \n\n", res.getTransactionID(), res.getPeer().getName());
                 //successful.add(response);
             } else {
            	 System.out.printf("Failed transaction proposal response Txid: %s from peer %s \n\n", res.getTransactionID(), res.getPeer().getName());
                 //failed.add(response);
             }
		 }
		
		 //CompletableFuture<TransactionEvent> cf = channel.sendTransaction(responses);
		 System.out.println("--- Before sendTransaction ----");
		 channel.sendTransaction(responses);
		 System.out.println("--- After sendTransaction ----");
		 
		 
		QueryByChaincodeRequest queryByChaincodeRequest = hfClient.newQueryProposalRequest();
        queryByChaincodeRequest.setArgs("a".getBytes(UTF_8)); // test using bytes as args. End2end uses Strings.
        queryByChaincodeRequest.setFcn("query");
        queryByChaincodeRequest.setChaincodeID(chaincodeID);

        Collection<ProposalResponse> queryProposals;

        try {
            queryProposals = channel.queryByChaincode(queryByChaincodeRequest);
        } catch (Exception e) {
            throw new CompletionException(e);
        }   

        for (ProposalResponse proposalResponse : queryProposals) {
        	if (!proposalResponse.isVerified() || proposalResponse.getStatus() != Status.SUCCESS) {
                System.out.printf("Got bad response from %s peer \n\n", proposalResponse.getPeer().getName());
            } else {
                String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                System.out.printf("Query payload of b from peer %s returned payload, %s \n\n", proposalResponse.getPeer().getName(), payload);        
            }
        }
		 
//		 QueryByChaincodeRequest queryRequest = hfClient.newQueryProposalRequest();
//		 queryRequest.setChaincodeID(ccid); // ChaincodeId object as created in Invoke block
//		 queryRequest.setFcn("queryCar"); // Chaincode function name for querying the blocks
//		
//		 String[] arguments1 = { "Toyota"}; // Arguments that the above functions take
//		 if (arguments1 != null)
//		  queryRequest.setArgs(arguments1);
//		
//		 // Query the chaincode  
//		 Collection<ProposalResponse> queryResponse = channel.queryByChaincode(queryRequest);
//		
//		 for (ProposalResponse pres : queryResponse) {
//		  // process the response here
//		 }
		
		System.out.println("\n\n------ End of main() ---------");	
	} // main ends here
	
	
	
	
	
	
	public void waste() throws Exception {
		
		// Fabric Gateway code
		String caUrl = "https://localhost:7054"; // ensure that port is of CA
		String caName = "ca-org1";
		//String ca_pemStr = "-----BEGIN CERTIFICATE-----\nMIICUjCCAfigAwIBAgIRALGn0YCp2GJiWLT7r6FLVckwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjAwMjEzMTAwMDAwWhcNMzAwMjEwMTAwMDAw\nWjBzMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UE\nAxMTY2Eub3JnMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\nBIrnhWWG1aXisvYskKPfGfR/nG9HeODZIyEY538K9Ecnr3hDSYN1m+cuIfw2ZLx/\n40sAqOiJBoT30cKyzSfJ5yKjbTBrMA4GA1UdDwEB/wQEAwIBpjAdBgNVHSUEFjAU\nBggrBgEFBQcDAgYIKwYBBQUHAwEwDwYDVR0TAQH/BAUwAwEB/zApBgNVHQ4EIgQg\nq5zej6VcG1xug3SINSt7smeL9i0ClmF9ki++zwKPedgwCgYIKoZIzj0EAwIDSAAw\nRQIhAIdzg5dTGjmzKergR+EgpBe9k+Dtsn74mTLzzI1ASmZqAiBFlJA+9xny4wyQ\nLQZ+xyBuCeWl8tfCROclrUPlBegnww==\n-----END CERTIFICATE-----\n";
		// ~/fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem
		//String ca_pemStr = "-----BEGIN CERTIFICATE-----\nMIICUjCCAfigAwIBAgIRAIxBLSaTCsMusIZffZVzqtQwCgYIKoZIzj0EAwIwczEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xGTAXBgNVBAoTEG9yZzEuZXhhbXBsZS5jb20xHDAaBgNVBAMTE2Nh\nLm9yZzEuZXhhbXBsZS5jb20wHhcNMjAwMjEzMTEzMTAwWhcNMzAwMjEwMTEzMTAw\nWjBzMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMN\nU2FuIEZyYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEcMBoGA1UE\nAxMTY2Eub3JnMS5leGFtcGxlLmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\nBNK4GfbR3bws4V2sh7aqLC6kxsMSUaWfN66AV32U9q3iNvF1MT9qdGnDOupN711V\nK0gs3lFn3QCwcXreXpS+0eqjbTBrMA4GA1UdDwEB/wQEAwIBpjAdBgNVHSUEFjAU\nBggrBgEFBQcDAgYIKwYBBQUHAwEwDwYDVR0TAQH/BAUwAwEB/zApBgNVHQ4EIgQg\nSRVWYbU2BUflr6WbaSWeBGs8v4KlrvjSWcHqR/gWeY4wCgYIKoZIzj0EAwIDSAAw\nRQIhAJnd6uqYKaTvCSeNCWadQBgM8XAqiP7X1TrMkKCE5jveAiAyQmTBF4gYss12\ndmfb6bbVfANmvqXiFha0rLrQHvmoNw==\n-----END CERTIFICATE-----\n";
	
	// ~/fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/tlsca/tlsca.org1.example.com-cert.pem	
	//String ca_pemStr = "-----BEGIN CERTIFICATE-----\nMIICVjCCAf2gAwIBAgIQFH2upqLGTJt6lJEOsuyxujAKBggqhkjOPQQDAjB2MQsw\nCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZy\nYW5jaXNjbzEZMBcGA1UEChMQb3JnMS5leGFtcGxlLmNvbTEfMB0GA1UEAxMWdGxz\nY2Eub3JnMS5leGFtcGxlLmNvbTAeFw0yMDAyMTMxMTMxMDBaFw0zMDAyMTAxMTMx\nMDBaMHYxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQH\nEw1TYW4gRnJhbmNpc2NvMRkwFwYDVQQKExBvcmcxLmV4YW1wbGUuY29tMR8wHQYD\nVQQDExZ0bHNjYS5vcmcxLmV4YW1wbGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0D\nAQcDQgAEX9dqTHsw+rTznHbfY+p2u/i0ao9iyssWp7JMnNNxtIJ0t7lRGaoHwUPZ\nynW3jmo6WAMM5QyXQSIWZOjq37Pt16NtMGswDgYDVR0PAQH/BAQDAgGmMB0GA1Ud\nJQQWMBQGCCsGAQUFBwMCBggrBgEFBQcDATAPBgNVHRMBAf8EBTADAQH/MCkGA1Ud\nDgQiBCBMiaVMXr/INaBnxQDrvYvwKSH0gFT3XiVv+Bg0do0x3jAKBggqhkjOPQQD\nAgNHADBEAiBHhjPOEUco1jy6ptR/GlJhBl9ZRQaHp+/SmbjJwyz9FQIgHcleKa60\nKiuz2e0EGZreS9Arf/J5lCBNYD6Mn6+IOR0=\n-----END CERTIFICATE-----\n";
	
		String caCertFileName = "/home/osboxes/fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";
		//String pemStr = "-----BEGIN CERTIFICATE-----\r\n****\r\n-----END CERTIFICATE-----\r\n";				
	    File caCertFile = new File(caCertFileName);
	    if (!caCertFile.exists() || !caCertFile.isFile()) {
	        throw new RuntimeException("CA Cert is missing cert file: " + caCertFile.getAbsolutePath());
	    }        
	    Properties caProperties = new Properties();
	    caProperties.setProperty("pemFile", caCertFile.getAbsolutePath());
	    caProperties.setProperty("allowAllHostNames", "true");
	    Path caPath = Paths.get(caCertFileName);        ;
		caProperties.put("pemBytes", Files.readAllBytes(caPath));

		//Properties ca_properties = new Properties();
		//ca_properties.put("pemBytes", ca_pemStr.getBytes());

		HFCAClient hfcaClient = HFCAClient.createNewInstance(caName, caUrl, caProperties);
        		
		CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		hfcaClient.setCryptoSuite(cryptoSuite);
		
		UserContext adminUserContext = new UserContext();
		adminUserContext.setName("admin"); // admin username
		adminUserContext.setAffiliation("department1"); // affiliation
		adminUserContext.setMspId("Org1MSP"); // org1 mspid
		
		Enrollment adminEnrollment = hfcaClient.enroll("admin", "adminpw"); //pass admin username and password
		adminUserContext.setEnrollment(adminEnrollment);
		
		Util.writeUserContext(adminUserContext); // save admin context to local file system

		UserContext userContext = new UserContext();
		userContext.setName("user1");
		userContext.setAffiliation("org1.department1");
		userContext.setMspId("Org1MSP");

		RegistrationRequest rr = new RegistrationRequest("user1", "org1.department1");
		//String enrollmentSecret = hfcaClient.register(rr, adminUserContext);

		//Enrollment enrollment = hfcaClient.enroll(userContext.getName(), enrollmentSecret);
		//userContext.setEnrollment(enrollment);
		//Util.writeUserContext(userContext);
		
		
		//UserContext adminUserContext1 = Util.readUserContext("org1", "admin");
		//CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
		HFClient hfClient = HFClient.createNewInstance();
		hfClient.setCryptoSuite(cryptoSuite);
		hfClient.setUserContext(adminUserContext);
		
		// Client properties (use admin user credentials)
		String clientTLSCertFileName = "/home/osboxes/fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/tls/client.crt";
		String clientKeyFileName = "/home/osboxes/fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/tls/client.key";
		File clientCertFile = new File(clientTLSCertFileName);
	    if (!clientCertFile.exists() || !clientCertFile.isFile()) {
	        throw new RuntimeException("Client Cert is missing cert file: " + clientCertFile.getAbsolutePath());
	    }
	    File clientKeyFile = new File(clientKeyFileName);
	    if (!clientKeyFile.exists() || !clientKeyFile.isFile()) {
	        throw new RuntimeException("Client key is missing: " + caCertFile.getAbsolutePath());
	    }
		
		String peer_name = "peer0.org1.example.com";
		String peer_url = "grpcs://localhost:7051"; // Ensure that port is of peer1
		//String peerTLSCertFileName = "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/msp/tlscacerts/tlsca.org1.example.com-cert.pem";
		//String peerTLSCertFileName = "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt";
		String peerTLSCertFileName = "/home/osboxes/fabric-samples/first-network/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/server.crt";
		//String peerTLSCertFileName = "crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/server.crt";
			
	    File peerCertFile = new File(peerTLSCertFileName);
	    if (!peerCertFile.exists() || !peerCertFile.isFile()) {
	        throw new RuntimeException("Peer Cert is missing cert file: " + peerCertFile.getAbsolutePath());
	    }        
	    Properties peerProperties = new Properties();
	    peerProperties.setProperty("pemFile", peerCertFile.getAbsolutePath());
	    peerProperties.setProperty("trustServerCertificate", "true");
	    //peerProperties.setProperty("pemFile", peerTLSCertFileName);
	    System.out.println("peerCertFile.getAbsolutePath(): " + peerCertFile.getAbsolutePath());
	    peerProperties.setProperty("allowAllHostNames", "true");
	    Path peerPath = Paths.get(peerTLSCertFileName);        ;
	    peerProperties.put("pemBytes", Files.readAllBytes(peerPath));
	    //peerProperties.put("pemBytes", peerPemStr.getBytes());
	    //System.out.println("Peer pem string: " + peerPemStr);
	    //peerProperties.setProperty("sslProvider", "JDK");
	    peerProperties.setProperty("sslProvider", "openSSL");
	    peerProperties.setProperty("negotiationType", "TLS");
	    peerProperties.setProperty("clientCertFile", clientCertFile.getAbsolutePath());
	    peerProperties.setProperty("clientKeyFile", clientKeyFile.getAbsolutePath());
	    Path clientCertFilePath = Paths.get(clientTLSCertFileName);        ;
	   	//peerProperties.put("clientCertBytes", Files.readAllBytes(clientCertFilePath));
	   	Path clientKeyFilePath = Paths.get(clientKeyFileName);        ;
	   	//peerProperties.put("clientKeyBytes", Files.readAllBytes(clientKeyFilePath));
	    peerProperties.setProperty("org.hyperledger.fabric.sdk.peer.organization_mspid", "Org1MSP");
	    //peerProperties.setProperty("negotiationType", "plainText");
		
		Peer peer = hfClient.newPeer(peer_name, peer_url, peerProperties);

		 String event_url = "grpcs://localhost:7053"; // ensure that port is of event hub
		 EventHub eventHub = hfClient.newEventHub(peer_name, event_url, peerProperties);

		String orderer_name = "orderer.example.com";
		 String orderer_url = "grpcs://localhost:7050"; // ensure that port is of orderer
		 //String ordererTLSCertFileName = "crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem";
		 String ordererTLSCertFileName = "/home/osboxes/fabric-samples/first-network/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt";
			//String pemStr = "-----BEGIN CERTIFICATE-----\r\n****\r\n-----END CERTIFICATE-----\r\n";				
	    File ordererCertFile = new File(ordererTLSCertFileName);
	    if (!ordererCertFile.exists() || !ordererCertFile.isFile()) {
	        throw new RuntimeException("Orderer Cert is missing cert file: " + ordererCertFile.getAbsolutePath());
	    }
		
		 Properties orderer_properties = new Properties();
		 orderer_properties.setProperty("pemFile", ordererCertFile.getAbsolutePath());
		 orderer_properties.setProperty("trustServerCertificate", "true");
		 //orderer_properties.setProperty("pemFile", ordererTLSCertFileName);
		 orderer_properties.setProperty("allowAllHostNames", "true");
		 Path ordererPath = Paths.get(peerTLSCertFileName);        ;
		 orderer_properties.put("pemBytes", Files.readAllBytes(ordererPath));
		 //orderer_properties.setProperty("sslProvider", "JDK");
		 orderer_properties.setProperty("sslProvider", "openSSL");
		 //orderer_properties.setProperty("negotiationType", "plainText");
		 orderer_properties.setProperty("negotiationType", "TLS");
		 orderer_properties.setProperty("oorg.hyperledger.fabric.sdk.orderer.organization_mspid", "OrdererMSP");
		 orderer_properties.setProperty("clientCertFile", clientCertFile.getAbsolutePath());
		 orderer_properties.setProperty("clientKeyFile", clientKeyFile.getAbsolutePath());
		 //orderer_properties.put("clientCertBytes", Files.readAllBytes(clientCertFilePath));
		 //orderer_properties.put("clientKeyBytes", Files.readAllBytes(clientKeyFilePath));
		 Orderer orderer = hfClient.newOrderer(orderer_name, orderer_url, orderer_properties);
		 
		 Channel channel = hfClient.newChannel("mychannel");

		 channel.addPeer(peer);
		 channel.addEventHub(eventHub);
		 channel.addOrderer(orderer);
		 channel.initialize();
		 
		 TransactionProposalRequest request = hfClient.newTransactionProposalRequest();
		 //String cc = "fabcarcc"; // Chaincode name
		 String cc = "mycc"; // Chaincode name
		 ChaincodeID ccid = ChaincodeID.newBuilder().setName(cc).build();

		 request.setChaincodeID(ccid);
		 //request.setFcn("initLedger"); // Chaincode invoke funtion name
		 request.setFcn("query"); // Chaincode invoke funtion name
		 //String[] arguments = { "CAR11", "VgW", "Poglo", "Ggrey", "Margy" }; // Arguments that Chaincode function takes
		 String[] arguments = { "a" };  // Arguments that Chaincode function takes
		 //request.setArgs(arguments);
		 request.setProposalWaitTime(3000);
		 Collection<ProposalResponse> responses = channel.sendTransactionProposal(request);
		 for (ProposalResponse res : responses) {
		   // Process response from transaction proposal
		 }
		
		 //CompletableFuture<TransactionEvent> cf = channel.sendTransaction(responses);
		 channel.sendTransaction(responses);
		 
		 QueryByChaincodeRequest queryRequest = hfClient.newQueryProposalRequest();
		 queryRequest.setChaincodeID(ccid); // ChaincodeId object as created in Invoke block
		 queryRequest.setFcn("queryCar"); // Chaincode function name for querying the blocks

		 String[] arguments1 = { "Toyota"}; // Arguments that the above functions take
		 if (arguments1 != null)
		  queryRequest.setArgs(arguments1);

		 // Query the chaincode  
		 Collection<ProposalResponse> queryResponse = channel.queryByChaincode(queryRequest);

		 for (ProposalResponse pres : queryResponse) {
		  // process the response here
		 }		 
	} // main function end
	

//	// Swagger Bean function
//	@Bean
//    public Docket SwaggerBeanAPI11() {
//            return new Docket(DocumentationType.SWAGGER_2).select()
//                    .apis(RequestHandlerSelectors.basePackage("com.example.OCIOHLFDXCClient")).build();
//    }
}
