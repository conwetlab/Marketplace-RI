<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="${ pageContext.request.contextPath }/resources/marketplace/js/views/descriptions/detail.js"></script>
<c:choose>
  <c:when test="${ not empty form_data }">
  <script>
    app.view.descriptionUpdateForm
    <c:forEach var="field" items="${ form_data }">
      .addInitialValue("${ field.key }", "${ field.value }")
    </c:forEach>
      .addErrorMessage("${ form_error.fieldName }", "${ form_error.fieldError }")
  </script>
  </c:when>
  <c:otherwise>
  <script>
    app.view.descriptionUpdateForm
      .addInitialValue("displayName", "${ description.displayName }")
      .addInitialValue("url", "${ description.url }")
      .addInitialValue("comment", "${ description.comment }")
  </script>
  </c:otherwise>
</c:choose>