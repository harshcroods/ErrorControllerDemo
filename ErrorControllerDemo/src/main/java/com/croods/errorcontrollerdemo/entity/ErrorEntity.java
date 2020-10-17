package com.croods.errorcontrollerdemo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "error_entity")
public class ErrorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "status_code")
	private Integer statusCode;

	@CreationTimestamp
	@Column(name = "create_date_time_stamp")
	private Date createDateTimeStamp;

	@Column(name = "error_details")
	private String errorDetails;
	
	@Column(name = "error_path")
	private String errorPath;

}
