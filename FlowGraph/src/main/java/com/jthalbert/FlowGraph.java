package com.jthalbert;


import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.Sail;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

import java.io.File;

/**
 * Hello world!
 *
 */
public class FlowGraph
{
    Repository repository = null;

    public FlowGraph() {
        this(false);
    }

    public FlowGraph(boolean inferencing) {
        try {
            if (inferencing) {
                repository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));

            } else {
                repository = new SailRepository(new MemoryStore());
            }
            repository.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }
    //RepositoryConnection connection
    public static void main( String[] args )
    {

    }
}
