<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script>

  WMarket.requests.register('storeList', function () {

    WMarket.currentStore = WMarket.resources.stores.getStoreByName("${ store.name }");

  });

</script>
<script src="${ pageContext.request.contextPath }/resources/marketplace/js/StoreDetailView.js"></script>
