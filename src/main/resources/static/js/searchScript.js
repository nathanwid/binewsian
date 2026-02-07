let searchTimeout;

$('#searchInput').on('input', function() {
    clearTimeout(searchTimeout);
    const searchValue = $(this).val().trim();

    searchTimeout = setTimeout(function() {
        if (searchValue.length >= 2 || searchValue.length === 0) {
            $('#searchForm').submit();
        }
    }, 500);
});

$('#searchForm').on('submit', function(e) {
    const searchValue = $('#searchInput').val().trim();
    if (searchValue.length === 1) {
        e.preventDefault();
        return false;
    }
});