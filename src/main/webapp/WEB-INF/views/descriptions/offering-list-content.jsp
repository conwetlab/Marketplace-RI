<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/descriptions/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-7">
    <div class="container-flex">
    <c:forEach var="offering" items="${ description.offerings }">

    <div class="panel panel-default-lighter offering-item">
      <div class="panel-heading text-center">
        <span class="image-thumbnail">
          <img class="image image-rounded image-bordered offering-image" src="${ offering.imageUrl }" />
        </span>
        <a class="panel-title" href="${ pageContext.request.contextPath }/offerings/${ description.store.name }/${ description.name }/${ offering.name }">${ offering.displayName }</a>
      </div>
      <div class="panel-body">
        <a class="offering-store" href="${ pageContext.request.contextPath }/stores/${ description.store.name }/offerings">Web Store</a>
        <div class="offering-description">${ offering.description }</div>
      </div>
    </div>

    </c:forEach>
    </div>
  </div>
</div>