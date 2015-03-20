<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row container-fluid">
  <div class="col-sm-10">
    <div class="alert alert-${ statusCode < 500 ? 'warning' : 'danger' }">
      <div class="alert-heading"><strong>${ statusCode } - ${ reasonPhrase }</strong></div>
      <div class="alert-content">${ content }</div>
    </div>
  </div>
</div>
