<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<c:set var="isOwner" value="${ store.creator.userName == user.userName }"></c:set>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/stores/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-5 col-lg-offset-1">
  <c:choose>
    <c:when test="${ isOwner }">

    <div class="panel panel-default">
      <div class="panel-heading text-center">
        <span class="panel-title">General information</span>
      </div>
      <div class="panel-body">
        <form class="col-md-8 col-md-offset-1" name="store_form" method="post" enctype="multipart/form-data" action="${ pageContext.request.contextPath }/stores/${ store.name }/about">
          <div class="form-field">
            <label class="field-label">Registration date</label>
            <div class="field-control static"><fmt:formatDate pattern="yyyy-MM-dd" value="${ store.registrationDate }" /></div>
          </div>
          <div class="form-options">
            <button type="submit" class="btn btn-success">
                <span class="btn-text">Save changes</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    </c:when>
    <c:otherwise>

    <div class="panel panel-default">
      <div class="panel-heading text-center">
        <span class="panel-title">General information</span>
      </div>
      <div class="panel-body">
        <div class="dl-group">
          <dl class="dl-block">
            <dt>Comment</dt>
            <c:choose>
              <c:when test="${ not empty store.comment }">
                <dd class="text-justify">${ store.comment }</dd>
              </c:when>
              <c:otherwise>
                <dd>No comment provided.</dd>
              </c:otherwise>
            </c:choose>
          </dl>
          <dl>
            <dt>Website URL</dt>
            <dd>${ store.url }</dd>
          </dl>
          <dl>
            <dt>Owner name</dt>
            <dd>${ store.creator.displayName }</dd>
          </dl>
          <dl>
            <dt>Registration date</dt>
            <dd><fmt:formatDate pattern="yyyy-MM-dd" value="${ store.registrationDate }" /></dd>
          </dl>
        </div>
      </div>
    </div>

    </c:otherwise>
  </c:choose>
  </div>
</div>