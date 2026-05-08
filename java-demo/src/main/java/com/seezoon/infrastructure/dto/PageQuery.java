package com.seezoon.infrastructure.dto;

public abstract class PageQuery {


    public static final int DEFAULT_PAGE_SIZE_LIMIT = 1000;

    /**
     * 页码
     */
    private int page = 1;
    /**
     * 每页条数
     */
    private int pageSize = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize > this.pageSizeLimit()) {
            throw new IllegalArgumentException("page size limit:" + this.pageSizeLimit() + ",current is " + pageSize);
        }
        this.pageSize = pageSize;
    }

    /**
     * 每页条数限制
     *
     * @return
     */
    public int pageSizeLimit() {
        return DEFAULT_PAGE_SIZE_LIMIT;
    }

}
