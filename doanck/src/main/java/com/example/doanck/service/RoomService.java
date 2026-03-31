package com.example.doanck.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Room;
import com.example.doanck.repository.RoomRepository;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;


    // lấy tất cả phòng
    public List<Room> getAllRooms(){
        return roomRepository.findAll();
    }


    // lưu phòng
    public Room save(Room room){
        return roomRepository.save(room);
    }


    // tìm phòng theo id
    public Room getRoomById(Long id){
        return roomRepository.findById(id).orElse(null);
    }


    // xóa phòng
    public void delete(Long id){
        roomRepository.deleteById(id);
    }

    public Room saveRoom(Room room){
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id){
        roomRepository.deleteById(id);
    }

}