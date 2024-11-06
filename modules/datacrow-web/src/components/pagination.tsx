import { Button } from "react-bootstrap";

type PaginationProps = {
	totalItems: number;
	itemsPerPage: number;
	currentPage: number;
	paginate: (pageNumber: number) => void;
};

export default function Pagination({
	totalItems,
	itemsPerPage,
	currentPage,
	paginate,
}: PaginationProps) {
	
	const totalPages = Math.ceil(totalItems / itemsPerPage);
	const startAt = currentPage > 20 ? currentPage - 10  : currentPage;
	const max = totalPages > 20 ? 20 : totalPages;
	const pageNumbers = Array.from(
		{ length: max},
		(_, index) => index + startAt
	);

	return (
		<div style={{ display: "flex", flexWrap: "wrap" }} id="pagination">
			{pageNumbers.map((number) => (
				<Button onClick={() => paginate(number)} className="pagination-button" key={"page" + number}>{number}</Button>
			))}
		</div>
	);
}
