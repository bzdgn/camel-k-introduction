import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.model.rest.RestBindingMode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Api extends RouteBuilder {

    private Map<Integer, Person> persons = new HashMap<>();

    @Override
    public void configure() throws Exception {
        
        // rest endpoints
        rest("/api/person/status")
            .get()
            .produces("application/json")
            .to("direct:status");
        
        rest("/api/person/{id}")
            .get()
            .produces("application/json")
            .to("direct:get-person");
        
        rest("/api/person/")
            .get()
            .produces("application/json")
            .to("direct:get-person-all");
        
        rest("/api/person/")
            .post()
            .type(Person.class)
            .bindingMode(RestBindingMode.json)
            .consumes("application/json")
            .produces("application/json")
            .to("direct:add-person");
        
        rest("/api/person/")
            .put()
            .type(Person.class)
            .bindingMode(RestBindingMode.json)
            .consumes("application/json")
            .produces("application/json")
            .to("direct:update-person");
        
        rest("/api/person/{id}")
            .delete()
            .produces("application/json")
            .to("direct:delete-person");
        
        // routes
        from("direct:status")
            .id("status-route")
            .log("Get request received for status")
            .bean(this, "handleStatus")
            .marshal().json()
            .log("Produced body: ${body}")
            ;
        
        from("direct:get-person")
            .id("get-route")
            .log("Get request received for id: ${header.id}")
            .bean(this, "handleGetPerson")
            .marshal().json()
            .log("Produced body: ${body}")
            ;
        
        from("direct:get-person-all")
            .id("get-all-route")
            .log("Get All request received")
            .bean(this, "handleGetPersons")
            .marshal().json()
            .log("Produced body: ${body}")
            ;
        
        from("direct:add-person")
            .id("add-route")
            .log("Post request received")
            .bean(this, "handleAddPerson")
            .marshal().json()
            .log("Produced body: ${body}")
            ;
        
        from("direct:update-person")
            .id("update-route")
            .log("Update request received")
            .bean(this, "handleUpdatePerson")
            .marshal().json()
            .log("Produced body: ${body}")
            ;
        
        from("direct:delete-person")
            .id("delete-route")
            .log("Delete request received for id: ${header.id}")
            .bean(this, "handleDeletePerson")
            .marshal().json()
            .log("Produced body: ${body}")
            ;
        
    }
    
    // beans
    public void handleStatus(Exchange exchange) {
        Response response = new Response("Status: Up and running!");
        exchange.getIn().setBody(response);
    }
    
    public void handleGetPerson(Exchange exchange) {
        Integer id = exchange.getIn().getHeader("id", Integer.class);
        
        Person p = getPerson(id);
        
        exchange.getIn().setBody(p);
    }
    
    public void handleGetPersons(Exchange exchange) {        
        Collection persons = getPersons();
        
        exchange.getIn().setBody(persons);
    }
    
    public void handleAddPerson(Exchange exchange) {
        Person p = exchange.getIn().getBody(Person.class);
        
        addPerson(p);
        
        Response response = new Response("Add successful");
        exchange.getIn().setBody(response);
    }
    
    public void handleUpdatePerson(Exchange exchange) {
        Person p = exchange.getIn().getBody(Person.class);
        
        updatePerson(p);
        
        Response response = new Response("Update successful");
        exchange.getIn().setBody(response);
    }
    
    public void handleDeletePerson(Exchange exchange) {
        Integer id = exchange.getIn().getHeader("id", Integer.class);
        
        deletePerson(id);
        
        Response response = new Response("Delete successful");
        exchange.getIn().setBody(response);
    }

    
    // CRUD methods
	
	public void addPerson(Person p) {
		persons.putIfAbsent(p.getId(), p);
	}
	
	public Person getPerson(int id) {
		if(persons.containsKey(id)) {
			return persons.get(id);
		}
		
		return null;
	}
	
	public Collection<Person> getPersons() {
		return persons.values();
	}
	
	public void updatePerson(Person p) {
		if (persons.containsKey(p.getId())) {
			persons.remove(p.getId());
			
			persons.put(p.getId(), p);
		}
	}
	
	public void deletePerson(int id) {
		if (persons.containsKey(id)) {
			persons.remove(id);
		}
	}
	
    public static class Person {
        private int id;
        private String name;
        
        public Person() {
        }
        
        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return id + " - " + name;
        }
    }
    
    public static class Response {
    	public String status;
        
        public Response() {
        }
    	
    	public Response(String status) {
    		this.status = status;
    	}
    	
    	@Override
    	public String toString() {
    		return "Status = " + status;
    	}
    }

}