package org.eclipse.microprofile.test.jwt;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class MySecureWalletTest extends Arquillian {

    /**
     * The base URL for the container under test
     */
    @ArquillianResource
    private URL baseURL;

    /**
     * Create a CDI aware JAX-RS application archive with our endpoints and
     * @return the JAX-RS application archive
     * @throws IOException - on resource failure
     */
    @Deployment(testable=true)
    public static WebArchive createDeployment() throws IOException {
        // Various system properties you can set to enable debug logging, debugging
        //System.setProperty("swarm.resolver.offline", "true");
        //System.setProperty("swarm.logging", "DEBUG");
        //System.setProperty("swarm.debug.port", "8888");

        // Get the
        URL publicKey = MySecureWalletTest.class.getResource("/publicKey.pem");
        WebArchive webArchive = ShrinkWrap
                .create(WebArchive.class, "MySecureEndpoint.war")
                //
                .addAsManifestResource(publicKey, "/MP-JWT-SIGNER")
                .addClass(MySecureWallet.class)
                .addClass(MyJaxrsApp.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("project-defaults.yml", "/project-defaults.yml")
                .addAsWebInfResource("jwt-roles.properties", "classes/jwt-roles.properties")
                .setWebXML("WEB-INF/web.xml")
                ;
        System.out.printf("WebArchive: %s\n", webArchive.toString(true));
        return webArchive;
    }

    @RunAsClient
    @Test(description = "Verify that jdoe can view balance using Token1.json")
    public void checkBalance() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token1.json");

        String uri = baseURL.toExternalForm() + "/wallet/balance";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri);
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        JsonObject reply = response.readEntity(JsonObject.class);
        Reporter.log(reply.toString());
        System.out.println(reply.toString());
    }
    @RunAsClient
    @Test(description = "Verify that jdoe can debit balance using Token1.json")
    public void debitBalance() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token1.json");

        String uri = baseURL.toExternalForm() + "/wallet/debit";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri)
                .queryParam("amount", "500");
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        JsonObject reply = response.readEntity(JsonObject.class);
        Reporter.log(reply.toString());
        System.out.println(reply.toString());
    }    @RunAsClient
    @Test(description = "Verify that jdoe can issue debit > $1000 using Token1.json")
    public void bigSpenderDebitBalance() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token1.json");

        String uri = baseURL.toExternalForm() + "/wallet/debit";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri)
                .queryParam("amount", "1500");
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        JsonObject reply = response.readEntity(JsonObject.class);
        Reporter.log(reply.toString());
        System.out.println(reply.toString());
    }
    @RunAsClient
    @Test(description = "Verify that jdoe can credit balance using Token1.json")
    public void creditBalance() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token1.json");

        String uri = baseURL.toExternalForm() + "/wallet/credit";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri)
                .queryParam("amount", "1500");
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        JsonObject reply = response.readEntity(JsonObject.class);
        Reporter.log(reply.toString());
        System.out.println(reply.toString());
    }
    @RunAsClient
    @Test(description = "Verify that jdoe can credit balance using Token1.json")
    public void bigDebitBalanceFail() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token1.json");

        String uri = baseURL.toExternalForm() + "/wallet/debit";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri)
                .queryParam("amount", "3000");
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_BAD_REQUEST);

        uri = baseURL.toExternalForm() + "/wallet/balance";
        echoEndpointTarget = ClientBuilder.newClient()
                .target(uri);
        response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        JsonObject reply = response.readEntity(JsonObject.class);
        Reporter.log(reply.toString());
        System.out.println(reply.toString());
    }
    @RunAsClient
    @Test(description = "Verify that jdoe2 cannot debit > 1000 using Token2.json")
    public void bigSpenderDebitBalanceFail() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token2.json");

        String uri = baseURL.toExternalForm() + "/wallet/debit";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri)
                .queryParam("amount", "1500");
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_FORBIDDEN);

        uri = baseURL.toExternalForm() + "/wallet/balance";
        echoEndpointTarget = ClientBuilder.newClient()
                .target(uri);
        response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        JsonObject reply = response.readEntity(JsonObject.class);
        Reporter.log(reply.toString());
        System.out.println(reply.toString());
    }

    @RunAsClient
    @Test(description = "Verify that jdoe3 has no access via Token-noaccess.json")
    public void checkBalanceNoAccess() throws Exception {
        Reporter.log("Begin checkBalance");
        String token = TokenUtils.generateTokenString("/Token-noaccess.json");

        String uri = baseURL.toExternalForm() + "/wallet/balance";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri);
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_FORBIDDEN);
    }
}
