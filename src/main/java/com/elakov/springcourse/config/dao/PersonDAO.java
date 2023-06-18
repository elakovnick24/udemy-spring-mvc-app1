package com.elakov.springcourse.config.dao;

import com.elakov.springcourse.config.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PersonDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Person> index() {
        return jdbcTemplate.query("SELECT * FROM Person", new BeanPropertyRowMapper<>(Person.class));
    }

    public Optional<Person> show(String email) {
        return jdbcTemplate.query("SELECT * FROM  Person WHERE email=?", new Object[]{email},
        new BeanPropertyRowMapper<>(Person.class)).stream().findAny();
    }

    public Person show(int id) {
        return jdbcTemplate.query("SELECT * FROM Person WHERE id=?",
                        new Object[]{id},
                        new BeanPropertyRowMapper<>(Person.class))
                .stream().findAny().orElse(null);
    }

    public void save(Person person) {
        jdbcTemplate.update("INSERT INTO Person(name, age, email, address) VALUES (1, ?, ?, ?)",
                person.getName(),
                person.getAge(),
                person.getEmail());
    }

    public void update(int id, Person updatedPerson) {
        jdbcTemplate.update("UPDATE Person SET name=?, age=?, email=?, address=? WHERE id=?",
                updatedPerson.getName(),
                updatedPerson.getAge(),
                updatedPerson.getEmail(),
                updatedPerson.getAddress(),
                id);
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM Person WHERE id=?", id);
    }

    ////////////////////////////////////
    //// Perfomance testing batch insert
    ////////////////////////////////////

    public void testMultipleUpdate() {
        List<Person> people = create1000People();

        long before = System.currentTimeMillis();

        for (Person person : people) {
            jdbcTemplate.update("INSERT INTO Person VALUES (?, ?, ?, ?)",
                    person.getId(),
                    person.getName(),
                    person.getAge(),
                    person.getEmail());
        }
        long after = System.currentTimeMillis();
        System.out.println("Time multiple update: " + (after - before));
    }

    public void testBatchUpdate() {
        List<Person> people = create1000People();
        long before = System.currentTimeMillis();

        jdbcTemplate.batchUpdate("INSERT INTO Person VALUES (?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setInt(1, people.get(i).getId());
                preparedStatement.setString(2, people.get(i).getName());
                preparedStatement.setInt(3, people.get(i).getAge());
                preparedStatement.setString(4, people.get(i).getEmail());
            }

            @Override
            public int getBatchSize() {
                return people.size();
            }
        });

        long after = System.currentTimeMillis();
        System.out.println("Time batch update: " + (after - before));
    }

    private List<Person> create1000People() {
        List<Person> people = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            people.add(new Person(i, "Name " + i, 30 + i, "test_" + i + "@email.com", "Some address"));
        }
        
        return people;
    }
}
