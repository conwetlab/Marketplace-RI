<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="panel panel-default">
  <div class="panel-heading text-center">
    <span class="image-thumbnail image-thumbnail-lg">
      <img class="image image-rounded image-bordered" src="${ offering.imageUrl }">
    </span>
    <div class="panel-title">${ offering.displayName }</div>
  </div>
  <div class="panel-body">
    <div class="tab-group tab-group-vertical">
      <div
        <c:choose>
          <c:when test="${ currentView == 'detail' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/offerings/${ offering.describedIn.store.name }/${ offering.describedIn.name }/${ offering.name }">
          <span class="fa fa-newspaper-o"></span>
          <span class="hidden-sm">General</span>
        </a>
      </div>
      <div
        <c:choose>
          <c:when test="${ currentView == 'priceplans' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/offerings/${ offering.describedIn.store.name }/${ offering.describedIn.name }/${ offering.name }/priceplans">
          <span class="fa fa-credit-card"></span>
          <span class="hidden-sm">Payment plans</span>
        </a>
      </div>
      <c:choose>
        <c:when test="${ not empty bookmark }">

          <div class="btn-toggle-bookmark tab tab-danger">
            <a href="javascript:app.view.toggleBookmark();">
              <span class="fa fa-bookmark"></span>
              <span class="hidden-sm">Remove bookmark</span>
            </a>
          </div>

        </c:when>
        <c:otherwise>

          <div class="btn-toggle-bookmark tab">
            <a href="javascript:app.view.toggleBookmark();">
              <span class="fa fa-bookmark"></span>
              <span class="hidden-sm">Add bookmark</span>
            </a>
          </div>

        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>