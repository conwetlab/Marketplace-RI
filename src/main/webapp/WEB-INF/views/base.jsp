<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>${ title }</title>
    <t:insertAttribute name="styles" />
  </head>
  <body>
    <t:insertAttribute name="navbar" />

    <c:if test="${ not empty message }">
    <div class="alert-manager alert-dismissible ">
      <div class="alert alert-success">
        <span class="fa fa-check-circle"></span> ${ message }
      </div>
    </div>
    </c:if>

    <t:insertAttribute name="panels" ignore="true" />
    <t:insertAttribute name="content" />
    <t:insertAttribute name="footer" />

    <t:insertAttribute name="scripts" />
    <t:insertAttribute name="scripts.extras" ignore="true" />
    <t:insertAttribute name="scripts.view" ignore="true" />
    <script> app.requests.dispatch('core'); </script>
  </body>
</html>