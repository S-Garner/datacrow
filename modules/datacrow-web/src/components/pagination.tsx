type PaginationProps = {
  totalPosts: number;
  itemsPerPage: number;
  paginate: (pageNumber: number) => void;
};

export default function Pagination({
  totalPosts,
  itemsPerPage,
  paginate,
}: PaginationProps) {
  const totalPages = Math.ceil(totalPosts / itemsPerPage);
  const pageNumbers = Array.from(
    { length: totalPages },
    (_, index) => index + 1
  );

  return (
    <nav>
      <ul style={{ display: "flex", flexWrap: "wrap" }}>
        {pageNumbers.map((number) => (
          <li
            key={number}
            className="text-center p-2 rounded text-xl outline w-[40px] hover:bg-gray-200 hover:text-gray-700"
          >
          
          
          
            <a onClick={() => paginate(number)} href="#" className="page-link">
              {number}
            </a>
          </li>
        ))}
      </ul>
    </nav>
  );
}
