package com.mobilecheckin.passengers.dao;

import java.util.Date;
import java.util.List;

import com.mobilecheckin.passengers.model.Passenger;

public interface PassengerDAO {
	
	public int save(Passenger psg);
	public Passenger get(String lastName, String resvNumber, String flightNumber);
	public List<Passenger> getList(String flightNumber, Date flightDate);
	public int update(Passenger psg);
	public int delete(Passenger psg);

}
