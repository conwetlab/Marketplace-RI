<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/descriptions/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-5 col-lg-offset-1">
    <div class="panel panel-default">
      <div class="panel-heading text-center">
        <span class="panel-title">General information</span>
      </div>
      <div class="panel-body">
        <form  class="col-md-8 col-md-offset-1" name="description_update_form" method="post" action="${ pageContext.request.contextPath }/stores/${ description.store.name }/descriptions/${ description.name }">
          <div class="form-field">
            <label class="field-label">Upload Date</label>
            <div class="field-control static"><fmt:formatDate pattern="yyyy-MM-dd" value="${ description.registrationDate }" /></div>
          </div>
          <div class="form-options">
            <button type="submit" class="btn btn-success">
              <span class="btn-text">Save changes</span>
            </button>
          </div>
        </form>
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
            <dd><a href="${ pageContext.request.contextPath }/stores/${ store.name }/offerings">${ store.displayName }</a></dd>
          </dl>
          <dl>
            <dt>Creator name</dt>
            <dd>${ store.creator.displayName }</dd>
          </dl>
          <dl>
            <dt>Registration date</dt>
            <dd><fmt:formatDate pattern="yyyy-MM-dd" value="${ store.registrationDate }" /></dd>
          </dl>
        </div>
      </div>
    </div>
  </div>
</div>