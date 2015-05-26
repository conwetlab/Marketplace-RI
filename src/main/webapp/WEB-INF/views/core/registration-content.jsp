<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-5">
    <h2>Create new WMarket Account</h2>
  </div>
  <div class="col-sm-10 col-md-6 col-lg-4">
    <div class="panel panel-default">
      <div class="panel-heading">
        <span class="image-thumbnail">
          <span class="image image-circle image-default-darker">
            <span class="fa fa-user fa-inverse"></span>
          </span>
        </span>
      </div>
      <div class="panel-body">
        <form class="col-md-8 col-md-offset-1" name="registration_form" method="post" action="${ pageContext.request.contextPath }/register">
          <div class="form-options">
            <button class="btn btn-warning" type="submit">
              <span class="btn-text">Create</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>
