import co.paralleluniverse.fibers.Suspendable
import com.template.ServiceState
import com.template.flows.RequestFlow
import com.template.flows.RequestServiceHistoryFlow
import com.template.flows.ServiceFlow
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.serialization.SerializationWhitelist
import net.corda.core.utilities.getOrThrow
import net.corda.webserver.services.WebServerPluginRegistry
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import java.util.function.Function
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

// *****************
// * API Endpoints *
// *****************
@Path("service")
class ServiceApi(val rpcOps: CordaRPCOps) {
    // Accessible at /api/template/templateGetEndpoint.
    @GET
    @Path("templateGetEndpoint")
    @Produces(MediaType.APPLICATION_JSON)
    fun templateGetEndpoint(): Response {
        return Response.ok("Template GET endpoint.").build()
    }

    @GET
    @Path("requestService")
    @Produces(MediaType.APPLICATION_JSON)
    fun requestService(@QueryParam(value = "mechanic") mechanic: String, @QueryParam(value = "registration") registration: String): Response {
        val mechanicX500 = CordaX500Name(mechanic, "London", "GB")
        val mechanicParty = rpcOps.wellKnownPartyFromX500Name(mechanicX500)
                ?: throw IllegalArgumentException("Unknown mechanic.")

        return try {
            val flowHandle = rpcOps.startFlow(::RequestFlow, mechanicParty, registration)
            val flowResult = flowHandle.returnValue.getOrThrow()
            // Return the response.
            Response.status(Response.Status.CREATED).entity("Request for service made: ${flowResult.tx.outputsOfType<ServiceState>().single()}.").build()
        } catch (e: Exception) {
            // For the purposes of this demo app, we do not differentiate by exception type.
            Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }
    }

    @GET
    @Path("carryOutService")
    @Produces(MediaType.APPLICATION_JSON)
    fun carryOutService(@QueryParam(value = "registration") registration: String, @QueryParam(value = "servicesDone") servicesDone: String): Response {
        return try {
            val flowHandle = rpcOps.startFlow(::ServiceFlow, registration, servicesDone)
            val flowResult = flowHandle.returnValue.getOrThrow()
            // Return the response.
            Response.status(Response.Status.CREATED).entity("Service carried out: ${flowResult.tx.outputsOfType<ServiceState>().single()}.").build()
        } catch (e: Exception) {
            // For the purposes of this demo app, we do not differentiate by exception type.
            Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }
    }

    @GET
    @Path("requestServiceHistory")
    @Produces(MediaType.APPLICATION_JSON)
    fun requestServiceHistory(@QueryParam(value = "owner") owner: String): Response {
        val ownerX500 = CordaX500Name(owner, "London", "GB")
        val ownerParty = rpcOps.wellKnownPartyFromX500Name(ownerX500)
                ?: throw IllegalArgumentException("Unknown owner or car.")

        return try {
            val flowHandle = rpcOps.startFlow(::RequestServiceHistoryFlow, ownerParty)
            val flowResult = flowHandle.returnValue.getOrThrow()
            // Return the response.
            Response.status(Response.Status.CREATED).entity("Service history: $flowResult.").build()
        } catch (e: Exception) {
            // For the purposes of this demo app, we do not differentiate by exception type.
            Response.status(Response.Status.BAD_REQUEST).entity(e.message).build()
        }
    }

}

// ***********
// * Plugins *
// ***********
class ServiceWebPlugin : WebServerPluginRegistry {
    // A list of lambdas that create objects exposing web JAX-RS REST APIs.
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::ServiceApi))
    //A list of directories in the resources directory that will be served by Jetty under /web.
    // This template's web frontend is accessible at /web/template.
    override val staticServeDirs: Map<String, String> = mapOf(
        // This will serve the serviceWeb directory in resources to /web/template
        "service" to javaClass.classLoader.getResource("serviceWeb").toExternalForm()
    )
}

// Serialization whitelist.
class TemplateSerializationWhitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(TemplateData::class.java)
}

// This class is not annotated with @CordaSerializable, so it must be added to the serialization whitelist, above, if
// we want to send it to other nodes within a flow.
data class TemplateData(val payload: String)
