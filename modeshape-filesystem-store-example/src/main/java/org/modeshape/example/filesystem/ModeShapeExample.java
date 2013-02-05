package org.modeshape.example.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Random;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;

public class ModeShapeExample {

    public static void main( String[] argv ) {

  

        // Create and start the engine ...
        ModeShapeEngine engine = new ModeShapeEngine();
        engine.start();

        // Load the configuration for a repository via the classloader (can also use path to a file)...
        Repository repository = null;
        String repositoryName = null;
        try {
            URL url = ModeShapeExample.class.getClassLoader().getResource("my-repository-config.json");
            RepositoryConfiguration config = RepositoryConfiguration.read(url);

            // We could change the name of the repository programmatically ...
            // config = config.withName("Some Other Repository");

            // Verify the configuration for the repository ...
            Problems problems = config.validate();
            if (problems.hasErrors()) {
                System.err.println("Problems starting the engine.");
                System.err.println(problems);
                System.exit(-1);
            }

            // Deploy the repository ...
            repository = engine.deploy(config);
            repositoryName = config.getName();
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        Session session = null;
        try {
            // Get the repository
            repository = engine.getRepository(repositoryName);

            // Create a session ...
            session = repository.login("default");

            // Get the root node ...
            Node root = session.getRootNode();
            Node nodeSource1 = root.getNode("source1");
            Node nodeSource2 = root.getNode("source2");
            addSampleNode(session, nodeSource1);
            addSampleNode(session, nodeSource2);
            session.save();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.logout();
            System.out.println("Shutting down engine ...");
            try {
                engine.shutdown().get();
                System.out.println("Success!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    
    private static void addSampleNode(Session session, Node node) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException, FileNotFoundException{
    	Random rnd = new Random();
    	Node file = node.addNode("createfile"+rnd.nextInt()+".mode", "nt:file");
        Node content = file.addNode("jcr:content", "nt:resource");
        content.setProperty("jcr:data", session.getValueFactory().createBinary(new ByteArrayInputStream(new byte[1000])));
        content.setProperty("jcr:encoding", "");
    }
}
