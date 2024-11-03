type PaginationProps = {
	totalItems: number;
	itemsPerPage: number;
	paginate: (pageNumber: number) => void;
};

export default function Pagination({
	totalItems,
	itemsPerPage,
	paginate,
}: PaginationProps) {
	const totalPages = Math.ceil(totalItems / itemsPerPage);
	const pageNumbers = Array.from(
		{ length: totalPages },
		(_, index) => index + 1
	);

	return (
		<div style={{ display: "flex", flexWrap: "wrap" }} id="pagination">
			{pageNumbers.map((number) => (
				<a onClick={() => paginate(number)} href="#" className="pagination-button" key={"page" + number}>{number}</a>
			))}
		</div>
	);
}
