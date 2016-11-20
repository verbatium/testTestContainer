package ee.valja7.tc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.OracleContainer;

import java.sql.*;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @Rule
    public OracleContainer oracle = new OracleContainer();
    private OracleDataSource oracleDataSource;

    @Before
    public void setUp() throws Exception {
        oracleDataSource = new OracleDataSource();
        oracleDataSource.setPassword(oracle.getPassword());
        oracleDataSource.setUser(oracle.getUsername());
        oracleDataSource.setURL(oracle.getJdbcUrl());
        oracleDataSource.setConnectionCachingEnabled(true);
    }

    @Test
    public void testSimple() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(oracle.getJdbcUrl());
        hikariConfig.setUsername(oracle.getUsername());
        hikariConfig.setPassword(oracle.getPassword());

        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Statement statement = ds.getConnection().createStatement();
        statement.execute("SELECT 1 FROM dual");
        ResultSet resultSet = statement.getResultSet();

        resultSet.next();
        int resultSetInt = resultSet.getInt(1);
        assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
    }

    @Test
    public void testOracleDriverVersion() throws Exception {
        Connection connection = oracleDataSource.getConnection();
        // Create Oracle DatabaseMetaData object
        DatabaseMetaData meta = connection.getMetaData();
        // check driver info:
        assertEquals("JDBC driver version is 11.1.0.7.0-Production", "11.1.0.7.0-Production", meta.getDriverVersion());
        connection.close();
    }

    @Test
    public void testLiquibaseScript() throws Exception {
        Connection connection = oracleDataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new liquibase.Liquibase("systemMaster.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }
}