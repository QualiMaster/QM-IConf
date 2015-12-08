package de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model;

/**
 * ENUM for the roles available within the application. 
 * Define all roles here.
 * 
 * @author Sass
 *
 */
public class ApplicationRole {
    
    public static final Role ADMIN = new Role("admin");
    
    public static final Role INFRASTRUCTURE_ADMIN = new Role("infrastructure_admin");
    
    public static final Role PIPELINE_DESIGNER = new Role("pipeline_designer");
    
    public static final Role ADAPTATION_MANAGER = new Role("adaptation_manager");

}
