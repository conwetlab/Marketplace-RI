<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="http://tiles.apache.org/tags-tiles" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-3">
    <t:insertTemplate template="/WEB-INF/views/users/header.jsp" />
  </div>
  <div class="col-sm-10 col-md-6 col-lg-5 col-lg-offset-1">
    <div class="panel panel-default">
      <div class="panel-heading text-center">
        <span class="panel-title">Change Password</span>
      </div>
      <div class="panel-body">
        <form class="col-md-8 col-md-offset-1" name="account_password_update_form" method="post" action="${ pageContext.request.contextPath }/account/password">
          <div class="form-options">
            <button type="submit" class="btn btn-success">
              <span class="btn-text">Save changes</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>