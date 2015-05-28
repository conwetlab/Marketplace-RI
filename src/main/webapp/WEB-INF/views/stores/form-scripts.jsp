<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="${ pageContext.request.contextPath }/resources/marketplace/js/views/stores/form.js"></script>
<c:choose>
  <c:when test="${ not empty form_data }">
  <script>
    app.view.storeForm
    <c:forEach var="field" items="${ form_data }">
      .addInitialValue("${ field.key }", "${ field.value }")
    </c:forEach>
      .addErrorMessage("${ form_error.fieldName }", "${ form_error.fieldError }")
  </script>
  </c:when>
  <c:when test="${ not empty store }">
  <script>
    app.view.storeForm
      .addInitialValue("displayName", "${ store.displayName }")
      .addInitialValue("url", "${ store.url }")
      .addInitialValue("comment", "${ store.comment }")
  </script>
  </c:when>
</c:choose>