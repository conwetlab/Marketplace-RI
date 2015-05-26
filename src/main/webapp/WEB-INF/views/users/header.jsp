<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="panel panel-default-darker">
  <div class="panel-heading text-center">
    <span class="image-thumbnail">
      <span class="image image-circle image-primary-ligther">
        <span class="fa fa-user fa-inverse"></span>
      </span>
    </span>
    <span class="panel-title">${ user.displayName }</span>
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
        <a href="${ pageContext.request.contextPath }/account">
          <span class="fa fa-newspaper-o"></span>
          <span class="hidden-sm">Personal</span>
        </a>
      </div>
      <c:if test="${ user.oauth2 eq false }">

      <div
        <c:choose>
          <c:when test="${ currentView == 'credentials' }">
            class="tab active"
          </c:when>
          <c:otherwise>
            class="tab"
          </c:otherwise>
        </c:choose>
      >
        <a href="${ pageContext.request.contextPath }/account/password">
          <span class="fa fa-key"></span>
          <span class="hidden-sm">Credentials</span>
        </a>
      </div>
      <div class="tab tab-danger">
        <a href="javascript:deleteAccount()">
          <span class="fa fa-trash"></span>
          <span class="hidden-sm">Delete account</span>
        </a>
      </div>

      </c:if>
    </div>
  </div>
</div>

<form name="account_delete_form" method="post" action="${ pageContext.request.contextPath }/account/delete">
  <!-- <p class="text-justify">This operation cannot be undone. All your stores, descriptions and offerings will be deleted too. Please be certain.</p> -->
</form>

<script>
  var deleteAccount = function deleteAccount() {
    document.account_delete_form.submit();
  };
</script>