<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container">
  <div class="col-sm-10 col-md-4 col-lg-5">
    <h2>Upload a new description</h2>
  </div>
  <div class="col-sm-10 col-md-6 col-lg-4">
    <div class="panel panel-default">
      <div class="panel-heading">
        <span class="image-thumbnail">
          <span class="image image-circle image-default-darker">
            <span class="fa fa-archive fa-inverse"></span>
          </span>
        </span>
      </div>
      <div class="panel-body">
        <c:choose>
          <c:when test="${ not empty storeList }">

          <form class="col-md-8 col-md-offset-1" name="description_create_form" method="post" action="${ pageContext.request.contextPath }/descriptions/register">
            <div class="form-options">
              <button type="submit" class="btn btn-warning">
                <span class="btn-text">Upload</span>
              </button>
            </div>
          </form>

          </c:when>
          <c:otherwise>

          <div class="alert alert-info">
            <span class="fa fa-info-circle"></span> To upload descriptions, there must be at least one store. Go to <a class="alert-link" href="${ pageContext.request.contextPath }/stores/register">register a store</a>.
          </div>

          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>