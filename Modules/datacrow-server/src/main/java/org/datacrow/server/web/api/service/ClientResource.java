package org.datacrow.server.web.api.service;

import java.util.List;

import org.datacrow.server.web.api.Client;
import org.datacrow.server.web.api.ClientMemoryRepository;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/clients")
public class ClientResource {

    private ClientMemoryRepository clientRepository = new ClientMemoryRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Client> getAll() {
        return clientRepository.getAll();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Client getById(@PathParam("id") Long id) {
        return clientRepository.getById(id);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Client insert(Client client) {
        return clientRepository.insert(client);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(Client client) {
        if (!clientRepository.exists(client.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(client.getId() + "Doesn't exists").build();
        }
        Client clie = clientRepository.update(client);
        return Response.ok().entity(clie).build();
    }

    @Path("/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Long id) {
        if (id == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalidad ID 0").build();
        }
        clientRepository.delete(id);
        return Response.ok().entity("Item has been deleted successfully.").build();
    }

}
