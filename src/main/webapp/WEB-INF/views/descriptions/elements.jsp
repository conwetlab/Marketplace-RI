<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:forEach var="description" items="${ descriptions }">
<div class="panel panel-default">
  <div class="panel-heading">
    <span class="image-thumbnail">
      <span class="image image-circle image-default-darker">
        <span class="fa fa-archive fa-inverse"></span>
      </span>
    </span>
  </div>
  <div class="panel-body">
    <div class="dl-group">
      <dl>
        <dt>Name</dt>
        <dd>
          <a href="${ pageContext.request.contextPath }/stores/${ description.store.name }/descriptions/${ description.name }">${ description.displayName }</a>
        </dd>
      </dl>
      <dl>
        <dt>Store</dt>
        <dd>
          <a href="${ pageContext.request.contextPath }/stores/${ description.store.name }/offerings">${ description.store.displayName }</a>
        </dd>
      </dl>
      <dl>
        <dt>URL to Linked USDL file</dt>
        <dd>${ description.url }</dd>
      </dl>
      <dl>
        <dt>Upload date</dt>
        <dd><fmt:formatDate pattern="yyyy-MM-dd" value="${ description.registrationDate }" /></dd>
      </dl>
    </div>
    <p class="text-bold">Offerings</p>
    <div class="description-offerings">
    <c:forEach var="offering" items="${ description.offerings }">

      <a class="offering-item" href="${ pageContext.request.contextPath }/offerings/${ description.store.name }/${ description.name }/${ offering.name }">
        <span class="offering-heading">
          <span class="image-thumbnail image-thumbnail-sm">
            <img class="image image-rounded" src="${ offering.imageUrl }" />
          </span>
        </span>
        <span class="offering-body">
          <span class="offering-name">${ offering.displayName }</span>
          <span class="offering-version">${ offering.version }</span>
        </span>
      </a>

    </c:forEach>
    </div>
  </div>
</div>
</c:forEach>