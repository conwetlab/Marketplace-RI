<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/stores/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-5 col-lg-offset-1">
    <c:choose>
    <c:when test="${ not empty descriptions }">
        <t:insertTemplate template="/WEB-INF/views/descriptions/elements.jsp" />
    </c:when>
    <c:otherwise>

    <div class="alert alert-warning">
      <span class="fa fa-exclamation-circle"></span> No description available.
    </div>

     </c:otherwise>
    </c:choose>
  </div>
</div>