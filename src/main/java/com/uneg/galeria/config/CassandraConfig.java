package com.uneg.galeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "com.uneg.galeria.history.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.data.cassandra.contact-points:cassandra:9042}")
    private String contactPoints;

    @Value("${spring.data.cassandra.local-dc:datacenter1}")
    private String localDataCenter;

    @Value("${spring.data.cassandra.keyspace-name:museo_history}")
    private String keyspaceName;

    @Override
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    protected String getLocalDataCenter() {
        return localDataCenter;
    }

    @Override
    public String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }
}