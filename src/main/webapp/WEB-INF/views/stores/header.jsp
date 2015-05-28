<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isOwner" value="${ store.creator.userName == user.userName }"></c:set>

<div class="panel panel-default">
  <div class="panel-heading text-center">
    <span class="image-thumbnail">
      <c:choose>
      <c:when test="${ not empty store.imagePath }">

        <img class="image image-circle" src="${ pageContext.request.contextPath }/${ store.imagePath }">

      </c:when>
      <c:otherwise>

        <span class="image image-circle image-default-darker">
          <span class="fa fa-building fa-inverse"></span>
        </span>

      </c:otherwise>
      </c:choose>
    </span>
    <span class="panel-title store-displayname">${ store.displayName }</span>
  </div>
  <div class="panel-body">
    <div class="tab-group tab-group-vertical">

      <div
        <c:choose>
          <c:when test="${ currentStoreView == 'offeringList' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/stores/${ store.name }/offerings">
          <span class="fa fa-cubes"></span>
          <span class="hidden-sm">All offerings</span>
        </a>
      </div><!-- /.tab -->

      <div
        <c:choose>
          <c:when test="${ currentStoreView == 'descriptionList' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/stores/${ store.name }/descriptions">
          <span class="fa fa-archive"></span>
          <span class="hidden-sm">My descriptions</span>
        </a>
      </div><!-- /.tab -->

      <div
        <c:choose>
          <c:when test="${ currentStoreView == 'detail' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/stores/${ store.name }/about">
          <span class="fa fa-newspaper-o"></span>
          <span class="hidden-sm">About</span>
        </a>
      </div><!-- /.tab -->

      <c:if test="${ isOwner }">

        <div class="tab tab-danger">
          <a href="javascript:deleteStore()">
            <span class="fa fa-trash"></span>
            <span class="hidden-sm">Delete store</span>
          </a>
        </div>

      </c:if>

    </div>
  </div>
</div>

<c:if test="${ isOwner }">

  <form name="store_delete_form" method="post" action="${ pageContext.request.contextPath }/stores/${ store.name }/delete">
    <!-- <p class="text-justify">This operation cannot be undone. All the offerings and descriptions that are contained in this store will be deleted too. Please be certain.</p> -->
  </form>

  <script>
    var deleteStore = function deleteStore() {
      document.store_delete_form.submit();
    };
  </script>

</c:if>
