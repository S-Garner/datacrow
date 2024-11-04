import { Button } from "react-bootstrap";

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
				<Button onClick={() => paginate(number)} className="pagination-button" key={"page" + number}>{number}</Button>
			))}
		</div>
	);
}
