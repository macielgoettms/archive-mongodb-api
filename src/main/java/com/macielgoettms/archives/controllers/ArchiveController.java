package com.macielgoettms.archives.controllers;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Controller
@RequestMapping("/archives")
public class ArchiveController {

  private final GridFS gridFS;

  @Autowired
  public ArchiveController(GridFS gridFS) {this.gridFS = gridFS;}

  @RequestMapping(method = RequestMethod.POST)
  public HttpEntity<String> post(@RequestParam("file") MultipartFile file) {
    try {
      GridFSInputFile gfsFile = gridFS.createFile(file.getInputStream());
      gfsFile.setContentType(file.getContentType());
      gfsFile.setFilename(file.getOriginalFilename());
      gfsFile.save();
      return new HttpEntity<>(gfsFile.toString());
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  

  @RequestMapping(path = "/{id:.+}", method = RequestMethod.GET)
  public HttpEntity<byte[]> get(@PathVariable("id") String id) {
    GridFSDBFile file = gridFS.findOne(new ObjectId(id));
    try {
      if (file != null) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        file.writeTo(byteArrayOutputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, file.getContentType());

        return new HttpEntity<>(byteArrayOutputStream.toByteArray(), headers);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.IM_USED);
    }
  }

  @RequestMapping(path = "/{id:.+}", method = RequestMethod.PUT)
  public HttpEntity<String> put(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) {
    try {
      gridFS.remove(new ObjectId(id));
      GridFSInputFile gfsFile = gridFS.createFile(file.getInputStream());
      gfsFile.setContentType(file.getContentType());
      gfsFile.setFilename(file.getOriginalFilename());
      gfsFile.save();
      return new HttpEntity<>(gfsFile.toString());
    } catch (IOException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  @RequestMapping(path = "/{id:.+}", method = RequestMethod.DELETE)
  public HttpEntity<byte[]> delete(@PathVariable("id") String id) {
     gridFS.remove(new ObjectId(id));
     return new ResponseEntity<>(HttpStatus.OK);
  }

}
