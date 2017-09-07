package org.eclipse.microprofile.test.jwt;

import java.math.BigDecimal;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
@DeclareRoles({"ViewBalance", "Debtor", "Creditor", "Debtor2", "BigSpender"})
@Path("/")
@DenyAll
public class MySecureWallet {
    private double bigSpenderLimit = 1000;
    private BigDecimal usdBalance = new BigDecimal("100000.0000");
    private BigDecimal bitcoinXrate = new BigDecimal("4538.0000");
    private BigDecimal ethereumXrate = new BigDecimal("328.0000");
    @Inject
    private JsonWebToken jwt;

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"ViewBalance", "Debtor", "Creditor"})
    public JsonObject getBalance() {
        return generateBalanceInfo();
    }

    @GET
    @Path("/debit")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"Debtor", "Debtor2"})
    public Response debit(@QueryParam("amount") String amount, @Context SecurityContext securityContext) {
        Double damount = Double.valueOf(amount);
        if(damount > bigSpenderLimit) {
            if(securityContext.isUserInRole("BigSpender")) {
                // Validate the spending limit from the token claim
                JsonNumber spendingLimit = jwt.getClaim("spendingLimit");
                if(spendingLimit == null || spendingLimit.doubleValue() < damount) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }
        usdBalance = usdBalance.subtract(new BigDecimal(amount));
        return Response.ok(generateBalanceInfo()).build();
    }

    @GET
    @Path("/credit")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("Creditor")
    public JsonObject credit(@QueryParam("amount") String amount) {
        usdBalance = usdBalance.add(new BigDecimal(amount));
        return generateBalanceInfo();
    }

    private JsonObject generateBalanceInfo() {
        JsonObject result = Json.createObjectBuilder()
                .add("usd", usdBalance)
                .add("bitcoin", usdBalance.divide(bitcoinXrate, BigDecimal.ROUND_HALF_EVEN))
                .add("ethereum", usdBalance.divide(ethereumXrate, BigDecimal.ROUND_HALF_EVEN))
                .build();
        return result;
    }
}
