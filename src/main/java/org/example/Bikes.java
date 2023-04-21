package org.example;


import jakarta.persistence.*;
import net.bytebuddy.dynamic.loading.InjectionClassLoader;

import java.util.Objects;
@Entity
@Table(name = "bikes")
public class Bikes {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "bike_id")
        private Integer id;

        @Column(name = "bike_model")
        private String model;

        // @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "place_id")
        private Integer place;

        @Column(name = "assigned_id")
        private Integer assigned_id;

        // getters y setters


    public Bikes() {
    }

    public Bikes(String model, Integer place, Integer assigned_id) {
        this.model = model;
        this.place = place;
        this.assigned_id = assigned_id;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getPlace() {
        return place;
    }

    public void setPlace(Integer place) {
        this.place = place;
    }

    public Integer getAssigned_id() {
        return assigned_id;
    }

    public void setAssigned_id(Integer assigned_id) {
        this.assigned_id = assigned_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bikes bikes = (Bikes) o;
        return Objects.equals(id, bikes.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}



