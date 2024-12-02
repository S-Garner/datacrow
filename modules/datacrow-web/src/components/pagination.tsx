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
	let max = totalPages > 10 ? 11 : totalPages;
	let startAt = currentPage < 5 ? 1 : currentPage - 5;

	if (startAt > 1 && currentPage + 5 > totalPages) {
		
		console.log("startAt1 " + startAt);
		
		startAt = startAt - ((currentPage + 5) - totalPages)
		
		console.log("startAt2 " + startAt);
	}
	
	const pageNumbers = Array.from(
		{ length: max},
		(_, index) => index + startAt
	);

	return (
		<div style={{ display: "flex", flexWrap: "wrap", marginTop: "1em" }} id="pagination">

			<Button onClick={() => paginate(1)} 
					className="pagination-button"
				 	key={"page-first"} style={{}}>
				«
			</Button>
			<Button onClick={() => paginate(currentPage - 1)} 
					className="pagination-button"
				 	key={"page-previous"} style={{}}>
				‹
			</Button>

			…
		
			{pageNumbers.map((number) => (
				<Button onClick={() => paginate(number)} 
						className={`${number === currentPage ? "pagination-button-selected" : "pagination-button"}`}
					 	key={"page" + number} style={{}}>
					{number}
				</Button>
			))}

			…
			
			<Button onClick={() => paginate(currentPage + 1)} 
					className="pagination-button"
				 	key={"page-next"} style={{}}>
				›
			</Button>
			<Button onClick={() => paginate(totalPages)} 
					className="pagination-button"
				 	key={"page-last"} style={{}}>
				»
			</Button>			
			
		</div>
	);
}
