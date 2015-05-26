<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="panel panel-default">
  <div class="panel-heading text-center">
    <span class="image-thumbnail">
      <span class="image image-circle image-default-darker">
        <span class="fa fa-archive fa-inverse"></span>
      </span>
    </span>
    <span class="panel-title">${ description.displayName }</span>
  </div>
  <div class="panel-body">
    <div class="tab-group tab-group-vertical">

      <div
        <c:choose>
          <c:when test="${ currentDescriptionView == 'detail' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/stores/${ description.store.name }/descriptions/${ description.name }">
          <span class="fa fa-newspaper-o"></span>
          <span class="hidden-sm">General</span>
        </a>
      </div>
      <div
        <c:choose>
          <c:when test="${ currentDescriptionView == 'offeringList' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/stores/${ description.store.name }/descriptions/${ description.name }/offerings">
          <span class="fa fa-cubes"></span>
          <span class="hidden-sm">Offerings</span>
        </a>
      </div>
      <div class="tab tab-danger">
        <a href="javascript:deleteDescription()">
          <span class="fa fa-trash"></span>
          <span class="hidden-sm">Delete description</span>
        </a>
      </div>
    </div>
  </div>
</div>

<form name="description_delete_form" method="post" action="${ pageContext.request.contextPath }/stores/${ description.store.name }/descriptions/${ description.name }/delete">
  <!-- <p class="text-justify">This operation cannot be undone. All the offerings are contained in this description will be deleted too. Please be certain.</p> -->
</form>

<script>
  var deleteDescription = function deleteDescription() {
    document.description_delete_form.submit();
  };
</script>