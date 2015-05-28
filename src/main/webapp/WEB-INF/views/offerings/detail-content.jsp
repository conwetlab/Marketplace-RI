<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/offerings/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-5 col-lg-offset-1">
    <div class="panel panel-default">
      <div class="panel-heading text-center">
        <div class="panel-title">General information</div>
      </div>
      <div class="panel-body">
        <div class="dl-group">
          <dl class="dl-block">
            <dt>Description</dt>
            <dd class="text-justify">${ offering.description }</dd>
          </dl>
          <dl>
            <dt>Version</dt>
            <dd>${ offering.version }</dd>
          </dl>
          <dl>
            <dt>Developer name</dt>
            <dd>${ offering.describedIn.creator.displayName }</dd>
          </dl>
          <dl>
            <dt>Upload date</dt>
            <dd><fmt:formatDate pattern="yyyy-MM-dd" value="${ offering.describedIn.registrationDate }" /></dd>
          </dl>
        </div>
      </div>
    </div>
    <div class="panel panel-default">
      <div class="panel-heading text-center">
        <span class="panel-title">Store information</span>
      </div>
      <div class="panel-body">
        <div class="dl-group">
          <dl>
            <dt>Name</dt>
            <dd><a href="${ pageContext.request.contextPath }/stores/${ offering.describedIn.store.name }/offerings">${ offering.describedIn.store.displayName }</a></dd>
          </dl>
          <dl>
            <dt>Creator name</dt>
            <dd>${ offering.describedIn.store.creator.displayName }</dd>
          </dl>
          <dl>
            <dt>Registration date</dt>
            <dd><fmt:formatDate pattern="yyyy-MM-dd" value="${ offering.describedIn.store.registrationDate }" /></dd>
          </dl>
        </div>
      </div>
    </div>
  </div>
</div>